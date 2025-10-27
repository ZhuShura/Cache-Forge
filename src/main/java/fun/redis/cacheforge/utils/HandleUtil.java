package fun.redis.cacheforge.utils;

import fun.redis.cacheforge.common.CacheForgeCodecException;

public class HandleUtil {
    public static final String ERROR = "ERROR";
    public static final String OK = "OK";
    public static final Boolean TRUE = true;
    public static final Boolean FALSE = false;

    public static void checkKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new CacheForgeCodecException("key为空");
        }
    }
}
