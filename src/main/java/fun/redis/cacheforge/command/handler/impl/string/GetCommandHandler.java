package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.generalizedInline.ErrorMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.SimpleStringMessage;
import fun.redis.cacheforge.storage.repo.StringStore;
import fun.redis.cacheforge.utils.HandleUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * get命令处理器
 * @author hua
 * @date 2025/10/27
 */
@Slf4j
public class GetCommandHandler implements ReadCommandHandler {
    @Override
    public void handle(Command command, ChannelHandlerContext ctx) {
        try {
            String key = command.getArgs()[0];
            HandleUtil.checkKey(key);
            ctx.writeAndFlush(new SimpleStringMessage(StringStore.get(key)));
        } catch (Exception e) {
            log.error("get命令异常{}", String.valueOf(e));
            ctx.writeAndFlush(new ErrorMessage(HandleUtil.ERROR));
        }
    }
}
