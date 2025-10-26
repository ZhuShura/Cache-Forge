package fun.redis.cacheforge.utils;

import io.netty.handler.codec.redis.FullBulkStringRedisMessage;
import io.netty.handler.codec.redis.IntegerRedisMessage;
import io.netty.handler.codec.redis.RedisMessage;
import io.netty.handler.codec.redis.SimpleStringRedisMessage;
import io.netty.util.CharsetUtil;

public class DecodeUtil {

    public static String decodeRedisMessage(RedisMessage msg) {
        if (msg instanceof FullBulkStringRedisMessage fMsg) {
            return fMsg.content().toString(CharsetUtil.UTF_8);
        } else if (msg instanceof SimpleStringRedisMessage sMsg) {
            return sMsg.content();
        } else if (msg instanceof IntegerRedisMessage iMsg) {
            return String.valueOf(iMsg.value());
        }
        return "";
    }
}
