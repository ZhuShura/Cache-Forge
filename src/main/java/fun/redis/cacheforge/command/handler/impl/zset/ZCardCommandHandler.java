package fun.redis.cacheforge.command.handler.impl.zset;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.model.ZSetValue;
import fun.redis.cacheforge.storage.repo.ZSetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentSkipListSet;

import static fun.redis.cacheforge.utils.MessageUtil.*;

@Slf4j
public class ZCardCommandHandler implements ReadCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length == 1) {
                String key = args[0];
                ConcurrentSkipListSet<ZSetValue> set = ZSetStore.get(key);
                if (set == null) {
                    set = new ConcurrentSkipListSet<>();
                }
                log.info("服务器返回: {}", set.size());
                ctx.writeAndFlush(toIntegerMessage(set.size()));
            } else {
                log.error("zcard命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("zcard命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }
}
