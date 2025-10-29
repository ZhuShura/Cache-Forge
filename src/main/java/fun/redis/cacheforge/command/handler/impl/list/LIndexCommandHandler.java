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
 * lindex命令处理器
 * @author hua
 * @date 2025/10/28
 */
@Slf4j
public class LIndexCommandHandler implements ReadCommandHandler {

    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length == 2) {
                String key = args[0];
                int index = Integer.parseInt(args[1]);
                List<String> list = ListStore.get(key);
                if (list == null) {
                    log.error("key不存在");
                    ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
                    return;
                }
                index = index < 0 ? list.size() + index : index;
                String value = list.get(index);
                ctx.writeAndFlush(value == null ?
                        FullBulkStringMessage.NULL_INSTANCE :
                        toFullBulkStringMessage(value));
                log.info("服务器返回: {}", list.size());
            } else {
                log.error("lindex命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(MessageUtil.Err.ERR));
            }
        } catch (Exception e) {
            log.error("lindex命令异常", e);
            ctx.writeAndFlush(toErrorMessage(MessageUtil.Err.ERR));
        }
    }
}
