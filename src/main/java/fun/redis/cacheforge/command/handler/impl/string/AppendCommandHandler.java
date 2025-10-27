package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.generalizedInline.ErrorMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.IntegerMessage;
import fun.redis.cacheforge.storage.repo.StringStore;
import fun.redis.cacheforge.utils.HandleUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppendCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(Command command, ChannelHandlerContext ctx) {
        try {
            String key = command.getArgs()[0];
            String value = command.getArgs()[1];
            
            HandleUtil.checkKey(key);
            
            int length = StringStore.append(key, value);
            ctx.writeAndFlush(new IntegerMessage(length));
        } catch (Exception e) {
            log.error("append命令异常: {}", e.getMessage());
            ctx.writeAndFlush(new ErrorMessage(HandleUtil.ERROR));
        }
    }
}