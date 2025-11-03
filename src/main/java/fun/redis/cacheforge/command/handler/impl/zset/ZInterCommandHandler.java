package fun.redis.cacheforge.command.handler.impl.zset;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.repo.ZSetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static fun.redis.cacheforge.utils.MessageUtil.*;

@Slf4j
public class ZInterCommandHandler implements ReadCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length < 2) {
                log.error("zinter命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
                return;
            }
            int currentIndex = 0;
            int numKeys = Integer.parseInt(args[currentIndex++]);
            if (numKeys < currentIndex || numKeys > args.length - 1) {
                log.error("numkeys错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
            List<String> keys = List.of(args).subList(currentIndex, numKeys + currentIndex);
            currentIndex += numKeys;

            boolean withScores = false;
            Aggregate aggregate = new Aggregate();
            List<Double> weights = new ArrayList<>();
            try {
                while (currentIndex < args.length) {
                    switch (Condition.valueOf(args[currentIndex].toUpperCase())) {
                        case WEIGHTS -> {
                            currentIndex++;
                            List<String> strWeights = List.of(args).subList(currentIndex, currentIndex + numKeys);
                            strWeights.stream().map(Double::parseDouble).forEach(weights::add);
                            currentIndex += numKeys;
                        }
                        case AGGREGATE -> {
                            currentIndex++;
                            AggregateType aggregateType = AggregateType.valueOf(args[currentIndex].toUpperCase());
                            switch (aggregateType) {
                                case SUM -> aggregate.isSum = true;
                                case MIN -> aggregate.isMin = true;
                                case MAX -> aggregate.isMax = true;
                                default -> {
                                    log.error("AGGREGATE只能为: SUM,MIN,MAX");
                                    ctx.writeAndFlush(toErrorMessage(Err.ERR));
                                }
                            }
                            currentIndex++;
                        }
                        case WITHSCORES -> {
                            currentIndex++;
                            withScores = true;
                        }
                        default -> currentIndex++;
                    }
                }
            } catch (Exception e) {
                log.error("zinter参数解析错误", e);
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }

            // 获取元素集合
            List<Map<String, Double>> memberToScoreMaps = keys.stream().map(ZSetStore::getMemberToScoreMap).toList();

            // 处理权重
            if (!weights.isEmpty()) {
                for (int i = 0; i < memberToScoreMaps.size(); i++) {
                    int index = i;
                    memberToScoreMaps.get(index).replaceAll((member, score) -> score * weights.get(index));
                }
            }

            // 提取交集并计算分数
            Map<String, Double> commonMembers = memberToScoreMaps.get(0);
            if (aggregate.isMin) {
                for (int i = 1; i < memberToScoreMaps.size(); i++) {
                    commonMembers = intersectWithMin(commonMembers, memberToScoreMaps.get(i));
                }
            } else if (aggregate.isMax) {
                for (int i = 1; i < memberToScoreMaps.size(); i++) {
                    commonMembers = intersectWithMax(commonMembers, memberToScoreMaps.get(i));
                }
            } else {
                for (int i = 1; i < memberToScoreMaps.size(); i++) {
                    commonMembers = intersectWithSum(commonMembers, memberToScoreMaps.get(i));
                }
            }

            List<String> result = new ArrayList<>();

            if (withScores) {
                commonMembers.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue())
                        .forEach(entry -> {
                            result.add(entry.getKey());
                            result.add(entry.getValue().toString());
                        });
            } else {
                commonMembers.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue())
                        .forEach(entry -> result.add(entry.getKey()));
            }

            log.info("服务器返回: {}", result);
            ctx.writeAndFlush(basicToArrayMessage(result));

        } catch (Exception e) {
            log.error("zinter命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }


    /**
     * 方式1: 取分数最小值 (MIN)
     */
    public static Map<String, Double> intersectWithMin(Map<String, Double> scoreMap1, Map<String, Double> scoreMap2) {
        Set<String> commonMembers = getCommonMembers(scoreMap1, scoreMap2);
        return commonMembers.stream()
                .collect(Collectors.toMap(
                        member -> member,
                        member -> Math.min(scoreMap1.get(member), scoreMap2.get(member))
                ));
    }

    /**
     * 方式2: 取分数最大值 (MAX)
     */
    public static Map<String, Double> intersectWithMax(Map<String, Double> scoreMap1, Map<String, Double> scoreMap2) {
        Set<String> commonMembers = getCommonMembers(scoreMap1, scoreMap2);
        return commonMembers.stream()
                .collect(Collectors.toMap(
                        member -> member,
                        member -> Math.max(scoreMap1.get(member), scoreMap2.get(member))
                ));
    }

    /**
     * 方式3: 分数求和 (SUM)
     */
    public static Map<String, Double> intersectWithSum(Map<String, Double> scoreMap1, Map<String, Double> scoreMap2) {
        Set<String> commonMembers = getCommonMembers(scoreMap1, scoreMap2);
        return commonMembers.stream()
                .collect(Collectors.toMap(
                        member -> member,
                        member -> scoreMap1.get(member) + scoreMap2.get(member)
                ));
    }

    /**
     * 获取交集中的成员
     */
    private static Set<String> getCommonMembers(Map<String, Double> map1, Map<String, Double> map2) {
        Set<String> common = new HashSet<>(map1.keySet());
        common.retainAll(map2.keySet());
        return common;
    }

    private enum Condition {
        WEIGHTS,
        AGGREGATE,
        WITHSCORES
    }

    private enum AggregateType {
        SUM,
        MIN,
        MAX
    }

    private static class Aggregate {
        boolean isSum = true;
        boolean isMin = false;
        boolean isMax = false;
    }
}
