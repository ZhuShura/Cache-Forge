package fun.redis.cacheforge.utils;

import fun.redis.cacheforge.common.CacheForgeCodecException;
import fun.redis.cacheforge.protocol.model.Message;
import fun.redis.cacheforge.protocol.model.array.ArrayMessage;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.storage.repo.StringStore;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HandleUtil {
    public static final String ERROR = "ERROR";
    public static final String OK = "OK";
    public static final Boolean TRUE = true;
    public static final Boolean FALSE = false;

    /**
     * 检查key是否为空
     * @param key key
     */
    public static void checkKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new CacheForgeCodecException("key为空");
        }
    }

    /**
     * 将字符串转换为ArrayMessage
     * @param value 值
     * @return ArrayMessage
     */
    public static ArrayMessage toArrayMessage(String value) {
        return new ArrayMessage(List.of(toFullBulkStringMessage(value)));
    }

    /**
     * 将字符串列表转换为ArrayMessage
     * @param values 值列表
     * @return ArrayMessage
     */
    public static ArrayMessage toArrayMessage(List<String> values) {
        List<Message> messages = new ArrayList<>();
        for (String value : values) {
            messages.add(toFullBulkStringMessage(value));
        }
        return new ArrayMessage(messages);
    }

    /**
     * 将字符串转换为FullBulkStringMessage
     * @param value 值
     * @return FullBulkStringMessage
     */
    public static FullBulkStringMessage toFullBulkStringMessage(String value) {
        return new FullBulkStringMessage(Unpooled.wrappedBuffer(value.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 判断字符串是否为数字
     * @param str 字符串
     * @return 是否为数字
     */
    public static Boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
