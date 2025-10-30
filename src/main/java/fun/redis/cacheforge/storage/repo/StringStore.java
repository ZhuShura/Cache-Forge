package fun.redis.cacheforge.storage.repo;

import fun.redis.cacheforge.common.CacheForgeCodecException;
import fun.redis.cacheforge.storage.model.StringEntry;
import fun.redis.cacheforge.utils.TimeUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static fun.redis.cacheforge.storage.repo.GlobalStore.*;

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
        if (getType(key) != null && !getType(key).equals(keyType.STRING.toString()))
            throw new CacheForgeCodecException("类型不匹配");
        STRING_MAP.put(key, new StringEntry(value));
        setType(key, keyType.STRING.toString());
    }


    /**
     * 获取字符串(过期自动删除)
     * @param key 键
     * @return 值
     */
    public static String get(String key) throws CacheForgeCodecException {
        StringEntry stringEntry = STRING_MAP.get(key);
        if (stringEntry == null) {
            return null;
        } else {
            if (TimeUtil.isExpire(key)) {
               STRING_MAP.remove(key, stringEntry);
               delExpire(key);
               return null;
            }
            if (!getType(key).equals(keyType.STRING.toString())) throw new CacheForgeCodecException("类型不匹配");
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
