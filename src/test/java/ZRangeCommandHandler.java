import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.model.ZSetValue;
import fun.redis.cacheforge.storage.repo.ZSetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * ZSet范围查询命令处理器
 * @author hua
 * @date 2025/10/30
 */
@Slf4j
public class ZRangeCommandHandler implements ReadCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length < 3) {
                log.error("zrange命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
                return;
            }

            String key = args[0];
            String startArg = args[1];
            String stopArg = args[2];

            ConcurrentSkipListSet<ZSetValue> values = ZSetStore.get(key);
            if (values == null || values.isEmpty()) {
                ctx.writeAndFlush(basicToArrayMessage(new ArrayList<>()));
                return;
            }

            // 解析选项参数
            RangeOptions options = parseOptions(Arrays.copyOfRange(args, 3, args.length));

            List<String> result;
            if (options.byScore) {
                result = rangeByScore(values, startArg, stopArg, options);
            } else if (options.byLex) {
                result = rangeByLex(values, startArg, stopArg, options);
            } else {
                result = rangeByRank(values, startArg, stopArg, options);
            }

            log.info("服务器返回: {}", result);
            ctx.writeAndFlush(basicToArrayMessage(result));
        } catch (Exception e) {
            log.error("zrange命令异常{}", String.valueOf(e));
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }

    private RangeOptions parseOptions(String[] optionArgs) {
        RangeOptions options = new RangeOptions();
        for (int i = 0; i < optionArgs.length; i++) {
            String option = optionArgs[i].toUpperCase();
            switch (option) {
                case "BYSCORE":
                    options.byScore = true;
                    break;
                case "BYLEX":
                    options.byLex = true;
                    break;
                case "REV":
                    options.rev = true;
                    break;
                case "WITHSCORES":
                    options.withScores = true;
                    break;
                case "LIMIT":
                    if (i + 2 < optionArgs.length) {
                        try {
                            options.limitOffset = Integer.parseInt(optionArgs[i + 1]);
                            options.limitCount = Integer.parseInt(optionArgs[i + 2]);
                            i += 2;
                        } catch (NumberFormatException e) {
                            // 忽略无效的LIMIT参数
                        }
                    }
                    break;
            }
        }
        return options;
    }

    private List<String> rangeByRank(ConcurrentSkipListSet<ZSetValue> values, String startArg, String stopArg, RangeOptions options) {
        int start, stop;
        try {
            start = Integer.parseInt(startArg);
            stop = Integer.parseInt(stopArg);
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }

        List<ZSetValue> valueList = new ArrayList<>(values);
        if (options.rev) {
            Collections.reverse(valueList);
        }

        // 处理负数索引
        if (start < 0) start = Math.max(0, valueList.size() + start);
        if (stop < 0) stop = Math.max(0, valueList.size() + stop);
        
        // 处理边界
        start = Math.max(0, start);
        stop = Math.min(valueList.size() - 1, stop);
        
        if (start > stop || start >= valueList.size()) {
            return new ArrayList<>();
        }

        List<ZSetValue> subList = valueList.subList(start, stop + 1);
        
        // 应用LIMIT选项
        if (options.hasLimit()) {
            int fromIndex = Math.min(subList.size(), options.limitOffset);
            int toIndex = Math.min(subList.size(), fromIndex + options.limitCount);
            if (fromIndex < toIndex) {
                subList = subList.subList(fromIndex, toIndex);
            } else {
                subList = new ArrayList<>();
            }
        }

        return buildResult(subList, options.withScores);
    }

    private List<String> rangeByScore(ConcurrentSkipListSet<ZSetValue> values, String startArg, String stopArg, RangeOptions options) {
        ScoreRange startRange = parseScoreRange(startArg);
        ScoreRange stopRange = parseScoreRange(stopArg);

        List<ZSetValue> filtered = values.stream()
                .filter(value -> {
                    double score = value.getScore();
                    boolean startMatch = startRange.inclusive ? 
                        score >= startRange.value : score > startRange.value;
                    boolean stopMatch = stopRange.inclusive ? 
                        score <= stopRange.value : score < stopRange.value;
                    return startMatch && stopMatch;
                })
                .collect(Collectors.toList());

        if (options.rev) {
            Collections.reverse(filtered);
        }

        // 应用LIMIT选项
        if (options.hasLimit()) {
            int fromIndex = Math.min(filtered.size(), options.limitOffset);
            int toIndex = Math.min(filtered.size(), fromIndex + options.limitCount);
            if (fromIndex < toIndex) {
                filtered = filtered.subList(fromIndex, toIndex);
            } else {
                filtered = new ArrayList<>();
            }
        }

        return buildResult(filtered, options.withScores);
    }

    private List<String> rangeByLex(ConcurrentSkipListSet<ZSetValue> values, String startArg, String stopArg, RangeOptions options) {
        LexRange startRange = parseLexRange(startArg);
        LexRange stopRange = parseLexRange(stopArg);

        List<ZSetValue> filtered = values.stream()
                .filter(value -> {
                    String member = value.getMember();
                    int startCompare = member.compareTo(startRange.value);
                    int stopCompare = member.compareTo(stopRange.value);
                    
                    boolean startMatch = true;
                    if (!startRange.inf && startRange.exclusive) {
                        startMatch = startCompare > 0;
                    } else if (!startRange.inf) {
                        startMatch = startCompare >= 0;
                    }
                    
                    boolean stopMatch = true;
                    if (!stopRange.inf && stopRange.exclusive) {
                        stopMatch = stopCompare < 0;
                    } else if (!stopRange.inf) {
                        stopMatch = stopCompare <= 0;
                    }
                    
                    return startMatch && stopMatch;
                })
                .collect(Collectors.toList());

        if (options.rev) {
            Collections.reverse(filtered);
        }

        // 应用LIMIT选项
        if (options.hasLimit()) {
            int fromIndex = Math.min(filtered.size(), options.limitOffset);
            int toIndex = Math.min(filtered.size(), fromIndex + options.limitCount);
            if (fromIndex < toIndex) {
                filtered = filtered.subList(fromIndex, toIndex);
            } else {
                filtered = new ArrayList<>();
            }
        }

        return buildResult(filtered, options.withScores);
    }

    private ScoreRange parseScoreRange(String arg) {
        ScoreRange range = new ScoreRange();
        if (arg.startsWith("(")) {
            range.inclusive = false;
            range.value = Double.parseDouble(arg.substring(1));
        } else if (arg.equals("-inf")) {
            range.value = Double.NEGATIVE_INFINITY;
            range.inclusive = true;
        } else if (arg.equals("+inf")) {
            range.value = Double.POSITIVE_INFINITY;
            range.inclusive = true;
        } else {
            range.inclusive = true;
            range.value = Double.parseDouble(arg);
        }
        return range;
    }

    private LexRange parseLexRange(String arg) {
        LexRange range = new LexRange();
        if (arg.equals("-")) {
            range.inf = true;
        } else if (arg.equals("+")) {
            range.inf = true;
        } else if (arg.startsWith("(")) {
            range.exclusive = true;
            range.value = arg.substring(1);
        } else if (arg.startsWith("[")) {
            range.value = arg.substring(1);
        } else {
            range.value = arg;
        }
        return range;
    }

    private List<String> buildResult(List<ZSetValue> values, boolean withScores) {
        List<String> result = new ArrayList<>();
        for (ZSetValue value : values) {
            result.add(value.getMember());
            if (withScores) {
                result.add(String.valueOf(value.getScore()));
            }
        }
        return result;
    }

    private static class RangeOptions {
        boolean byScore = false;
        boolean byLex = false;
        boolean rev = false;
        boolean withScores = false;
        int limitOffset = 0;
        int limitCount = Integer.MAX_VALUE;

        boolean hasLimit() {
            return limitOffset > 0 || limitCount < Integer.MAX_VALUE;
        }
    }

    private static class ScoreRange {
        double value;
        boolean inclusive = true;
    }

    private static class LexRange {
        String value = "";
        boolean exclusive = false;
        boolean inf = false;
    }
}
