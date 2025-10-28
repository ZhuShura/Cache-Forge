package fun.redis.cacheforge.command.handler.impl;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.generalizedInline.SimpleStringMessage;
import fun.redis.cacheforge.protocol.model.pool.FixedMessagePool;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command ) {
        ctx.writeAndFlush(new SimpleStringMessage("OK"));
    }
}
