package fun.redis.cacheforge.command.handler.impl;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.generalizedInline.ErrorMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.IntegerMessage;
import fun.redis.cacheforge.storage.repo.StringStore;
import fun.redis.cacheforge.utils.HandleUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DelCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(Command command, ChannelHandlerContext ctx) {
        try {
            String key = command.getArgs()[0];
            HandleUtil.checkKey(key);
            if (StringStore.del(key)) {
                ctx.writeAndFlush(new IntegerMessage(1));
            } else {
                ctx.writeAndFlush(new IntegerMessage(0));
            }
        } catch (Exception e) {
            log.error("del命令异常{}", String.valueOf(e));
            ctx.writeAndFlush(new ErrorMessage(HandleUtil.ERROR));
        }
    }
}
