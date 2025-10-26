package fun.redis.cacheforge.server;

import fun.redis.cacheforge.config.ServerConfig;
import fun.redis.cacheforge.handler.MessageHandler;
import fun.redis.cacheforge.protocol.RespDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.redis.RedisArrayAggregator;
import io.netty.handler.codec.redis.RedisDecoder;
import io.netty.handler.codec.redis.RedisEncoder;

/**
 * redis服务启动类
 * @author hua
 * @date 2025/10/25
 */
public class RedisServer {
    public static void start() {
        ServerConfig config = new ServerConfig();
        ServerBootstrap b = new ServerBootstrap().group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new RespDecoder())
                                .addLast(new RedisEncoder())
                                .addLast(new MessageHandler());
                    }
                });
        ChannelFuture future = b.bind(config.PORT);
        future.addListener(f -> {
            if (f.isSuccess()) {
                System.out.println("服务成功启动于 " + config.PORT);
            } else {
                System.out.println("服务启动失败");
            }
        });
    }

}
