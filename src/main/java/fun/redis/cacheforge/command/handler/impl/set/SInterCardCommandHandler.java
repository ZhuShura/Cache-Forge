package fun.redis.cacheforge.command.handler.impl.set;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.repo.SetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static fun.redis.cacheforge.utils.MessageUtil.*;

@Slf4j
public class SInterCardCommandHandler implements ReadCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length >= 2) {
                int numKeys = Integer.parseInt(args[0]);
                if (numKeys < 1 || numKeys > args.length - 1) {
                    log.error("sintercard命令参数无效");
                    ctx.writeAndFlush(toErrorMessage(Err.ERR));
                    return;
                }
                List<String> keys = List.of(args).subList(1, numKeys + 1);
                List<Set<String>> sets = new ArrayList<>();
                for (String key : keys) {
                    Set<String> set = SetStore.get(key);
                    if (set == null) {
                        ctx.writeAndFlush(toIntegerMessage(0));
                        return;
                    }
                    sets.add(set);
                }
                // 从小到大取交集
                sets.sort(Comparator.comparingInt(Set::size));
                int limit = 0;
                if (args.length - numKeys == 3) {
                    LIMIT l = LIMIT.valueOf(args[numKeys + 1].toUpperCase());
                    if (l != LIMIT.LIMIT) {
                        log.error("sintercard命令参数无效");
                        ctx.writeAndFlush(toErrorMessage(Err.ERR));
                        return;
                    }
                    limit = Integer.parseInt(args[numKeys + 2]);
                } else if (args.length - numKeys != 1) {
                    log.error("limit参数数量错误");
                    ctx.writeAndFlush(toErrorMessage(Err.ERR));
                    return;
                }
                if (limit < 0) {
                    log.error("limit参数不能为负数");
                    ctx.writeAndFlush(toErrorMessage(Err.ERR));
                    return;
                }
                int intersectionSize = calculateIntersectionSize(sets, limit);
                log.info("服务器返回: {}", intersectionSize);
                ctx.writeAndFlush(toIntegerMessage(intersectionSize));
            } else {
                log.error("sintecard命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("sintercard命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }

    /**
     * 计算多个集合的交集大小，支持LIMIT优化
     */
    private int calculateIntersectionSize(List<Set<String>> sets, int limit) {
        if (sets.isEmpty()) return 0;

        // 使用第一个（最小）集合作为基准
        Set<String> smallestSet = sets.get(0);
        int count = 0;

        // 遍历最小集合中的每个元素
        for (String element : smallestSet) {
            boolean inAllSets = true;

            // 检查元素是否在所有其他集合中
            for (int i = 1; i < sets.size(); i++) {
                if (!sets.get(i).contains(element)) {
                    inAllSets = false;
                    break;
                }
            }

            if (inAllSets) {
                count++;

                // LIMIT 优化：达到限制提前返回
                if (limit > 0 && count >= limit) {
                    return limit;
                }
            }
        }

        return count;
    }

    private enum LIMIT {
        LIMIT
    }
}
