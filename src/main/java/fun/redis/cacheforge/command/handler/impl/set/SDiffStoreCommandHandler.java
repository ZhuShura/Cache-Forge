package fun.redis.cacheforge.command.handler.impl.set;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.repo.SetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static fun.redis.cacheforge.utils.MessageUtil.*;

@Slf4j
public class SDiffStoreCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length >= 2) {
                String destination = args[0];
                String firstKey = args[1];
                List<String> keys = List.of(args).subList(2, args.length);
                Set<String> firstSet = SetStore.get(firstKey);
                if (firstSet == null) {
                    firstSet = new HashSet<>();
                }
                for (String key : keys) {
                    Set<String> otherSet = SetStore.get(key);
                    if (otherSet != null) {
                        firstSet.removeAll(otherSet);
                    }
                }
                Set<String> set = new HashSet<>(firstSet);
                SetStore.set(destination, set);
                List<String> result = new ArrayList<>(set);
                log.info("服务器返回: {}", result);
                ctx.writeAndFlush(basicToArrayMessage(result));
            } else {
                log.error("sdiffstore命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("sdiffstore命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }
}
