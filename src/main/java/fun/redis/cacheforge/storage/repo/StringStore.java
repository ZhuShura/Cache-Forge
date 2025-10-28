package fun.redis.cacheforge.storage.repo;

import fun.redis.cacheforge.common.CacheForgeConstants;
import fun.redis.cacheforge.storage.model.StringEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字符串存储仓库
 * @author huangtaiji
 * @date 2025/10/27
 */
public class StringStore {
    private static final Map<String, StringEntry> STRING_MAP = new ConcurrentHashMap<>();

    /**
     * 插入字符串
     * @param key 键
     * @param value 值
     */
    public static void set(String key, String value) {
        STRING_MAP.put(key, new StringEntry(value));
    }

    /**
     * 插入字符串,并设置过期时间
     * @param key 键
     * @param value 值
     * @param expireUnit 时间单位
     * @param expireTime 过期时间
     */
    public static void set(String key, String value, CacheForgeConstants.ExpireUnit expireUnit, Long expireTime) {
        STRING_MAP.put(key, new StringEntry(value, expireTime*expireUnit.value()));
    }

    /**
     * 获取字符串
     * @param key 键
     * @return 值
     */
    public static String get(String key) {
        if (STRING_MAP.get(key) == null) {
            return null;
        } else {
            StringEntry stringEntry = STRING_MAP.get(key);
            if (stringEntry.isExpired()) {
                STRING_MAP.remove(key);
                return null;
            }
            return stringEntry.getValue();
        }
    }

    /**
     * 删除字符串
     * @param key 键
     * @return 是否删除成功
     */
    public static boolean del(String key) {
        return STRING_MAP.remove(key, STRING_MAP.get(key));
    }


}
