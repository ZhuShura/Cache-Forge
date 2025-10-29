package fun.redis.cacheforge.command.handler.impl.set;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.repo.SetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

import static fun.redis.cacheforge.utils.MessageUtil.*;

@Slf4j
public class SCardCommandHandler implements ReadCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length == 1) {
                String key = args[0];
                Set<String> set = SetStore.get(key);
                int result = set == null ? 0 : set.size();
                log.info("服务器返回: {}", result);
                ctx.writeAndFlush(toIntegerMessage(result));
            } else {
                log.error("scard命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("scard命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }
}
