package fun.redis.cacheforge.handler;

import fun.redis.cacheforge.client.RedisClient;
import fun.redis.cacheforge.client.ServerClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ServerClient.removeClient(ctx.channel());
        log.info("客户端下线{}", ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        RedisClient client = new RedisClient(ctx.channel(), ctx.channel().remoteAddress());
        ServerClient.addClient(ctx.channel(), client);
        log.info("客户端上线{}", ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(String.valueOf(cause));
    }
}