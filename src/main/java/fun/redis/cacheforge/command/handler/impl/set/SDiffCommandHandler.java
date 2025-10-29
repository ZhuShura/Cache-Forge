package fun.redis.cacheforge.command.handler.impl.set;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.repo.SetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * sdiff命令处理器
 * @author hua
 * @date 2025/10/29
 */
@Slf4j
public class SDiffCommandHandler implements ReadCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length >= 1) {
                String firstKey = args[0];
                List<String> keys = List.of(args).subList(1, args.length);
                Set<String> set = SetStore.get(firstKey);
                if (set == null) {
                    set = new HashSet<>();
                }
                for (String key : keys) {
                    Set<String> otherSet = SetStore.get(key);
                    if (otherSet != null) {
                        set.removeAll(otherSet);
                    }
                }
                List<String> result = new ArrayList<>(set);
                log.info("服务器返回: {}", result);
                ctx.writeAndFlush(basicToArrayMessage(result));
            } else {
                log.error("sdiff命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("sdiff命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }
}
