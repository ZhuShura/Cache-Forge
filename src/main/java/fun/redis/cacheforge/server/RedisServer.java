package fun.redis.cacheforge.server;

import fun.redis.cacheforge.config.ServerConfig;
import fun.redis.cacheforge.handler.ClientHandler;
import fun.redis.cacheforge.handler.MessageHandler;
import fun.redis.cacheforge.protocol.ArrayAggregator;
import fun.redis.cacheforge.protocol.BulkStringAggregator;
import fun.redis.cacheforge.protocol.RespDecoder;
import fun.redis.cacheforge.protocol.RespEncoder;
import fun.redis.cacheforge.utils.WordArtUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * redis服务启动类
 * @author hua
 * @date 2025/10/25
 */
@Slf4j
public class RedisServer {
    public static void start() {
        ServerConfig config = new ServerConfig();
        ServerBootstrap b = new ServerBootstrap().group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new ClientHandler())
                                .addLast(new RespDecoder())
                                .addLast(new RespEncoder())
                                .addLast(new BulkStringAggregator())
                                .addLast(new ArrayAggregator())
                                .addLast(new MessageHandler());
                    }
                });
        ChannelFuture future = b.bind(config.PORT);
        future.addListener(f -> {
            if (f.isSuccess()) {
                WordArtUtil.wordArtDraw();
                log.warn("服务成功启动于 {}", config.PORT);
            } else {
                log.error("服务启动失败");
            }
        });
    }

}
