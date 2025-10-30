package fun.redis.cacheforge.command.handler.impl.zset;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.model.ZSetValue;
import fun.redis.cacheforge.storage.repo.ZSetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * ZSet添加命令处理器
 * @author hua
 * @date 2025/10/30
 */
@Slf4j
public class ZAddCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length >= 3) {
                String key = args[0];
                ConcurrentSkipListSet<ZSetValue> values = ZSetStore.get(key);
                Map<String, Double> memberToScore = ZSetStore.getMemberToScoreMap(key);
                if (values == null) {
                    values = new ConcurrentSkipListSet<>();
                }
                if (memberToScore == null) {
                    memberToScore = new ConcurrentHashMap<>();
                }
                List<Condition> conditions = new ArrayList<>();
                int index = 1;
                // 添加所有条件
                while (index < args.length && isOption(args[index])) {
                    conditions.add(Condition.valueOf(args[index].toUpperCase()));
                    index ++;
                }
                // 校验条件
                if (!validate(conditions)) {
                    log.error("NX, XX, GT, LT互相排斥");
                    ctx.writeAndFlush(toErrorMessage(Err.ERR));
                }
                // 校验剩余参数, 必须为偶数
                int remainingArgs = args.length - index;
                if (remainingArgs % 2 != 0) {
                    log.error("zadd命令参数数量错误");
                    ctx.writeAndFlush(toErrorMessage(Err.ERR));
                    return;
                }
                // 解析score-member对
                ConcurrentSkipListSet<ZSetValue> newValues = new ConcurrentSkipListSet<>(values);
                Map<String, Double> newMemberToScore = new ConcurrentHashMap<>(memberToScore);
                for (int i = index; i < args.length; i += 2) {
                    try {
                        double score = Double.parseDouble(args[i]);
                        String member = args[i + 1];
                        if (conditions.contains(Condition.XX)) {
                            if (!memberToScore.containsKey(member)) {
                                continue;
                            }
                        } else if (conditions.contains(Condition.NX)) {
                            if (memberToScore.containsKey(member)) {
                                continue;
                            }
                        }
                        if (conditions.contains(Condition.GT)) {
                            if (memberToScore.containsKey(member) && score <= memberToScore.get(member)) {
                                continue;
                            }
                        } else if (conditions.contains(Condition.LT)) {
                            if (memberToScore.containsKey(member) && score >= memberToScore.get(member)) {
                                continue;
                            }
                        }
                        if (conditions.contains(Condition.INCR)) {
                            if (memberToScore.containsKey(member)) {
                                score += memberToScore.get(member);
                            }
                        }
                        newValues.add(new ZSetValue(member, score));
                        newMemberToScore.put(member, score);
                    } catch (NumberFormatException e) {
                        log.error("score只能为数字");
                        ctx.writeAndFlush(toErrorMessage(Err.ERR));
                        return;
                    }
                }
                ZSetStore.set(key, newValues, newMemberToScore);
                if (!conditions.contains(Condition.CH)) {
                    log.info("服务器返回{}", newValues.size() - values.size());
                    ctx.writeAndFlush(toIntegerMessage(newValues.size() - values.size()));
                } else {
                    newValues.removeAll(values);
                    List<String> result = new ArrayList<>();
                    for (ZSetValue newValue : newValues) {
                        result.add(newValue.getMember());
                        result.add(String.valueOf(newValue.getScore()));
                    }
                    log.info("服务器返回{}", result);
                    ctx.writeAndFlush(basicToArrayMessage(result));
                }
            } else {
                log.error("zadd命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("zadd命令异常{}", String.valueOf(e));
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }

    /**
     * 校验条件
     * @param conditions 条件
     * @return 是否合法
     */
    private boolean validate(List<Condition> conditions) {
        long mutualExclusiveCount = Stream.of(Condition.NX, Condition.XX, Condition.GT, Condition.LT)
                .filter(conditions::contains)
                .count();
        return mutualExclusiveCount <= 1;
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

    private enum Condition {
        NX,XX,GT,LT,CH,INCR
    }
}
