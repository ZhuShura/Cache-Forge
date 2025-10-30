package fun.redis.cacheforge.command.handler.impl.zset;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.model.ZSetValue;
import fun.redis.cacheforge.storage.repo.ZSetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * ZSet差集命令处理器
 * @author hua
 * @date 2025/10/30
 */
@Slf4j
public class ZDiffCommandHandler implements ReadCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length < 2) {
                log.error("zdiff命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
                return;
            }

            int numKeys;
            try {
                numKeys = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                log.error("zdiff命令第一个参数必须是数字");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
                return;
            }

            if (numKeys <= 0 || numKeys >= args.length) {
                log.error("zdiff命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
                return;
            }

            if (1 + numKeys >= args.length) {
                log.error("zdiff命令缺少key参数");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
                return;
            }

            // 提取所有key
            List<String> keys = new ArrayList<>(Arrays.asList(args).subList(1, numKeys + 1));

            // 获取WITHSCORES选项
            boolean withScores = false;
            if (args.length > 1 + numKeys) {
                withScores = "WITHSCORES".equalsIgnoreCase(args[1 + numKeys]);
            }

            // 执行zdiff操作
            List<String> result = computeZDiff(keys, withScores);
            
            log.info("服务器返回: {}", result);
            ctx.writeAndFlush(basicToArrayMessage(result));
        } catch (Exception e) {
            log.error("zdiff命令异常{}", String.valueOf(e));
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }

    private List<String> computeZDiff(List<String> keys, boolean withScores) {
        if (keys.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取第一个集合
        ConcurrentSkipListSet<ZSetValue> firstSet = ZSetStore.get(keys.get(0));
        if (firstSet == null || firstSet.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> firstMembers = new HashSet<>();

        for (ZSetValue value : firstSet) {
            firstMembers.add(value.getMember());
        }

        // 从第二个集合开始，移除在其他集合中存在的元素
        for (int i = 1; i < keys.size(); i++) {
            ConcurrentSkipListSet<ZSetValue> otherSet = ZSetStore.get(keys.get(i));
            if (otherSet != null && !otherSet.isEmpty()) {
                for (ZSetValue value : otherSet) {
                    firstMembers.remove(value.getMember());
                }
            }
        }

        // 构建结果
        List<String> result = new ArrayList<>();
        // 按照第一个集合中的顺序排列
        for (ZSetValue value : firstSet) {
            if (firstMembers.contains(value.getMember())) {
                result.add(value.getMember());
                if (withScores) {
                    result.add(String.valueOf(value.getScore()));
                }
            }
        }

        return result;
    }
}
