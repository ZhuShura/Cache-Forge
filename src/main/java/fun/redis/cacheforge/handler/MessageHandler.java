package fun.redis.cacheforge.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.redis.*;
import io.netty.util.ReferenceCountUtil;

import java.util.ArrayList;
import java.util.List;

public class
MessageHandler extends SimpleChannelInboundHandler<RedisMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RedisMessage msg) throws Exception {
        try {
            if (msg instanceof ArrayRedisMessage) {
                handleArrayMessage(ctx, (ArrayRedisMessage) msg);
            } else {
                handleSimpleMessage(ctx, msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void handleArrayMessage(ChannelHandlerContext ctx, ArrayRedisMessage msg) {
        List<RedisMessage> children = msg.children();
        if (children == null || children.isEmpty()) {
            ctx.writeAndFlush(new ErrorRedisMessage("ERR empty command"));
            return;
        }

        ByteBuf command = ((FullBulkStringRedisMessage)children.get(0)).content();
        List<String> args = new ArrayList<>();
        for (int i = 1; i < children.size(); i++) {
            args.add(DecodeUtil.decodeRedisMessage(children.get(i)));
        }

        System.out.println("Received command: " + command + " with args: " + args);
        // executeCommand(command, args);
    }

    private void handleSimpleMessage(ChannelHandlerContext ctx, RedisMessage msg) {
        if (msg instanceof ErrorRedisMessage) {
            System.out.println("Received error: " + ((ErrorRedisMessage) msg).content());
        } else if (msg instanceof SimpleStringRedisMessage) {
            System.out.println("Received string: " + ((SimpleStringRedisMessage) msg).content());
        } else if (msg instanceof IntegerRedisMessage) {
            System.out.println("Received integer: " + ((IntegerRedisMessage) msg).value());
            ctx.writeAndFlush(msg); // Echo back
        } else if (msg instanceof FullBulkStringRedisMessage) {
            System.out.println("Received bulk string: " + ((FullBulkStringRedisMessage) msg).content());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}