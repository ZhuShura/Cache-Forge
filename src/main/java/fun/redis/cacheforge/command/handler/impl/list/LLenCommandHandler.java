package fun.redis.cacheforge.command.handler.impl.list;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.storage.repo.ListStore;
import fun.redis.cacheforge.utils.MessageUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * llen命令处理器
 * @author hua
 * @date 2025/10/28
 */
@Slf4j
public class LLenCommandHandler implements ReadCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length == 1) {
                String key = args[0];
                List<String> list = ListStore.get(key);
                if (list == null) {
                    log.error("key不存在");
                    ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
                    return;
                }
                ctx.writeAndFlush(toIntegerMessage(list.size()));
                log.info("服务器返回: {}", list.size());
            } else {
                log.error("llen命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(MessageUtil.Err.ERR));
            }
        } catch (Exception e) {
            log.error("llen命令异常", e);
            ctx.writeAndFlush(toErrorMessage(MessageUtil.Err.ERR));
        }
    }
}
