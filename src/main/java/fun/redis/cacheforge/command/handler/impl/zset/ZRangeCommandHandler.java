package fun.redis.cacheforge.command.handler.impl.zset;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.common.CacheForgeCodecException;
import fun.redis.cacheforge.protocol.model.array.ArrayMessage;
import fun.redis.cacheforge.storage.model.ZSetValue;
import fun.redis.cacheforge.storage.repo.ZSetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import static fun.redis.cacheforge.utils.MessageUtil.*;

@Slf4j
public class ZRangeCommandHandler implements ReadCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();

            if (args.length < 3 || args.length > 9) {
                log.error("zrange命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }

            String key = args[0];
            String start = args[1];
            String end = args[2];
            ConcurrentSkipListSet<ZSetValue> zSet = ZSetStore.get(key);
            if (zSet == null) {
                log.info("key不存在");
                ctx.writeAndFlush(ArrayMessage.EMPTY_INSTANCE);
                return;
            }

            // 处理条件
            int index = 3;
            int offset = 0;
            int count = zSet.size();
            List<Condition> conditions = new ArrayList<>();
            while (index < args.length) {
                if (isOption(args[index])) {
                    Condition condition = Condition.valueOf(args[index].toUpperCase());
                    conditions.add(condition);
                    index++;
                    if (condition == Condition.LIMIT) {
                        offset = Integer.parseInt(args[index]);
                        index++;
                        count = Integer.parseInt(args[index]);
                        index++;
                    }
                }
            }

            // 核心逻辑
            List<String> result;
            if (conditions.contains(Condition.BYSCORE)) {
                result = rangeByScore(zSet, start, end, conditions, offset, count);
            } else if (conditions.contains(Condition.BYLEX)) {
                result = rangeByLex(zSet, start, end, conditions, offset, count);
            } else {
                result = rangeByRank(zSet, start, end, conditions, offset, count);
            }
            log.info("服务器返回: {}", result);
            ctx.writeAndFlush(basicToArrayMessage(result));
        } catch (Exception e) {
            log.error("zcard命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }

    /**
     *  获取指定区间的元素
     */
    private List<String> rangeByRank(ConcurrentSkipListSet<ZSetValue> zSet,
                                        String start,
                                        String end,
                                        List<Condition> conditions,
                                        int offset,
                                        int count) {
        int startIndex = Integer.parseInt(start);
        int endIndex = Integer.parseInt(end);

        List<ZSetValue> valueList = new ArrayList<>(zSet);
        if (conditions.contains(Condition.REV)) {
            Collections.reverse(valueList);
        }
        // 处理负数索引
        startIndex = startIndex < 0 ? zSet.size() + startIndex : startIndex;
        endIndex = endIndex < 0 ? zSet.size() + endIndex : endIndex;

        startIndex = Math.max(0, startIndex);
        endIndex = Math.min(valueList.size() - 1, endIndex);

        if (endIndex < startIndex || startIndex > valueList.size()) {
            return new ArrayList<>();
        }
        valueList = valueList.subList(startIndex, endIndex + 1);

        return processingWithScore(processingLimit(valueList, offset, count, conditions.contains(Condition.LIMIT)),
                conditions.contains(Condition.WITHSCORES));
    }

    /**
     * 获取以字符为索引的指定区间的元素
     */
    private List<String> rangeByLex(ConcurrentSkipListSet<ZSetValue> values,
                                       String start,
                                       String end,
                                       List<Condition> conditions,
                                       int offset,
                                       int count) {
        LexRange startRange = parseLexRange(start, true);
        LexRange stopRange = parseLexRange(end, false);

        List<ZSetValue> filtered = values.stream()
                .filter(value -> {
                    String member = value.getMember();
                    int startCompare = member.compareTo(startRange.value); //获取起始位置
                    int stopCompare = member.compareTo(stopRange.value); //获取结束位置

                    // 判断起始位置是否在区间内
                    boolean startMatch = true;
                    if (!startRange.inf && startRange.exclusive) {
                        startMatch = startCompare > 0;
                    } else if (!startRange.inf) {
                        startMatch = startCompare >= 0;
                    }

                    // 判断结束位置是否在区间内
                    boolean stopMatch = true;
                    if (!stopRange.inf && stopRange.exclusive) {
                        stopMatch = stopCompare < 0;
                    } else if (!stopRange.inf) {
                        stopMatch = stopCompare <= 0;
                    }

                    // 过滤出都满足的成员
                    return startMatch && stopMatch;
                })
                .collect(Collectors.toList());

        if (conditions.contains(Condition.REV)) {
            Collections.reverse(filtered);
        }

        return processingWithScore(processingLimit(filtered, offset, count, conditions.contains(Condition.LIMIT)),
                conditions.contains(Condition.WITHSCORES));
    }

    private List<String> rangeByScore(ConcurrentSkipListSet<ZSetValue> values,
                                      String start,
                                      String end,
                                      List<Condition> conditions,
                                      int offset,
                                      int count) {
        int startNum = 0;
        boolean startInclusive;
        if (start.startsWith("(")) {
            startInclusive = false;
            startNum = Integer.parseInt(start.substring(1));
        } else {
            startInclusive = true;
            if (start.equals("-inf")) {
                startNum = Integer.MIN_VALUE;
            } else if (start.equals("+inf")) {
                startNum = Integer.MAX_VALUE;
            } else {
                startNum = Integer.parseInt(start);
            }
        }
        int endNum = 0;
        boolean endInclusive;
        if (end.endsWith(")")) {
            endInclusive = false;
            endNum = Integer.parseInt(end.substring(0, end.length() - 1));
        } else {
            endInclusive = true;
            if (end.equals("-inf")) {
                endNum = Integer.MIN_VALUE;
            } else if (end.equals("+inf")) {
                endNum = Integer.MAX_VALUE;
            } else {
                endNum = Integer.parseInt(end);
            }
        }

        int finalStartNum = startNum;
        int finalEndNum = endNum;
        List<ZSetValue> filtered = values.stream()
                .filter(value -> {
                    double score = value.getScore();
                    boolean startMatch = startInclusive ?
                            score >= finalStartNum : score > finalStartNum;
                    boolean stopMatch = endInclusive ?
                            score <= finalEndNum : score < finalEndNum;
                    return startMatch && stopMatch;
                })
                .collect(Collectors.toList());

        if (conditions.contains(Condition.REV)) {
            Collections.reverse(filtered);
        }

        return processingWithScore(processingLimit(filtered, offset, count, conditions.contains(Condition.LIMIT)),
                conditions.contains(Condition.WITHSCORES));
    }

    /**
     * 处理limit参数
     */
    private List<ZSetValue> processingLimit(List<ZSetValue> valueList, int offset, int count, boolean hasLimit) {
        if (!hasLimit) {
            return valueList;
        }
        return valueList.subList(offset, offset + count);
    }

    /**
     * 处理withscores参数
     */
    private List<String> processingWithScore(List<ZSetValue> valueList, boolean withScores) {
        List<String> result = new ArrayList<>();
        for (ZSetValue value : valueList) {
            result.add(value.getMember());
            if (withScores) {
                result.add(value.getScore().toString());
            }
        }
        return result;
    }

    private LexRange parseLexRange(String arg, boolean isStart) {
        LexRange range = new LexRange();
        if (arg.equals("-")) {
            range.inf = true;
        } else if (arg.equals("+")) {
            range.inf = true;
        } else if (isStart) {
            if (arg.startsWith("(")) {
                range.exclusive = true;
                range.value = arg.substring(1);
            }
            else if (arg.startsWith("[")) {
                range.value = arg.substring(1);
            } else throw new CacheForgeCodecException("参数错误");
        } else {
            if (arg.endsWith(")")) {
                range.exclusive = true;
                range.value = arg.substring(0, arg.length() - 1);
            } else if (arg.endsWith("[")) {
                range.value = arg.substring(0, arg.length() - 1);
            } else throw new CacheForgeCodecException("参数错误");
        }
        return range;
    }

    /**
     * 判断是否为条件参数
     * @param arg 参数
     * @return 是否为条件参数
     */
    private boolean isOption(String arg) {
        try {
            Condition.valueOf(arg.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static class LexRange {

        String value = "";
        boolean exclusive = false;
        boolean inf = false;
    }
    
    private enum Condition {
        BYSCORE,
        BYLEX,
        REV,
        LIMIT,
        WITHSCORES
    }
}
