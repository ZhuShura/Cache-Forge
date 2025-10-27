package fun.redis.cacheforge.client;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerClient {
    private static ConcurrentHashMap<Channel, RedisClient> redisClients;

    public static Map<Channel, RedisClient> getRedisClients() {
        return redisClients;
    }

    public static RedisClient getClientByChannel(Channel channel) {
        return redisClients.get(channel);
    }

    public static void addClient(Channel channel, RedisClient client) {
        if (redisClients == null) {
            // 最大客户端连接数
            redisClients = new ConcurrentHashMap<>(128);
        }
        redisClients.put(channel, client);
    }

    public static void removeClient(Channel channel) {
        redisClients.remove(channel);
    }
}
