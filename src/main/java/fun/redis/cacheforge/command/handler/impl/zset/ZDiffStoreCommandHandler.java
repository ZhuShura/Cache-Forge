package fun.redis.cacheforge.command.handler.impl.zset;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.model.ZSetValue;
import fun.redis.cacheforge.storage.repo.ZSetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * ZSet差集存储命令处理器
 *
 * @author hua
 * @date 2025/10/30
 */
@Slf4j
public class ZDiffStoreCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length < 3) {
                log.error("zdiffstore命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
                return;
            }

            String destinationKey = args[0];

            int numKeys;
            try {
                numKeys = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                log.error("zdiffstore命令第二个参数必须是数字");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
                return;
            }

            if (numKeys <= 0 || numKeys > args.length - 2) {
                log.error("zdiffstore命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
                return;
            }

            // 提取所有源key
            List<String> sourceKeys = new ArrayList<>(Arrays.asList(args).subList(2, 2 + numKeys));

            // 执行zdiff操作
            ConcurrentSkipListSet<ZSetValue> result = computeZDiff(sourceKeys);

            // 存储结果到目标key
            Map<String, Double> memberToScore = new HashMap<>();
            for (ZSetValue value : result) {
                memberToScore.put(value.getMember(), value.getScore());
            }
            ZSetStore.set(destinationKey, result, memberToScore);
            // 返回结果集合的元素数量
            int resultSize = result.size();
            log.info("服务器返回: {}", resultSize);
            ctx.writeAndFlush(toIntegerMessage(resultSize));
        } catch (Exception e) {
            log.error("zdiffstore命令异常{}", String.valueOf(e));
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }

    private ConcurrentSkipListSet<ZSetValue> computeZDiff(List<String> keys) {
        if (keys.isEmpty()) {
            return new ConcurrentSkipListSet<>();
        }

        // 获取第一个集合
        ConcurrentSkipListSet<ZSetValue> firstSet = ZSetStore.get(keys.get(0));
        if (firstSet == null || firstSet.isEmpty()) {
            return new ConcurrentSkipListSet<>();
        }

        // 创建结果集合，初始化为第一个集合的副本
        ConcurrentSkipListSet<ZSetValue> result = new ConcurrentSkipListSet<>(firstSet);

        // 从第二个集合开始，移除在其他集合中存在的元素
        for (int i = 1; i < keys.size(); i++) {
            ConcurrentSkipListSet<ZSetValue> otherSet = ZSetStore.get(keys.get(i));
            if (otherSet != null && !otherSet.isEmpty()) {
                for (ZSetValue value : otherSet) {
                    result.removeIf(v -> v.getMember().equals(value.getMember()));
                }
            }
        }

        return result;
    }
}
