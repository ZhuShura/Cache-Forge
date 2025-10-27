package fun.redis.cacheforge.client;

import fun.redis.cacheforge.command.model.Command;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.SocketAddress;

@Data
@NoArgsConstructor
public class RedisClient {
    SocketAddress address;
    Channel channel;

    /**
     * 客户端名称
     */
    String name;

    /**
     * 客户端当前发送的命令
     */
    Command currentCommand;

    /**
     * 输入输出缓冲区
     */
    ByteBuf buf;

    public RedisClient(Channel channel, SocketAddress address) {
        this.channel = channel;
        this.address = address;
    }



}
