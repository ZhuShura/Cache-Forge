package fun.redis.cacheforge.storage.repo;

import fun.redis.cacheforge.common.CacheForgeCodecException;
import fun.redis.cacheforge.storage.model.ZSetEntry;
import fun.redis.cacheforge.storage.model.ZSetValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static fun.redis.cacheforge.storage.repo.GlobalStore.*;

/**
 * ZSet存储仓库
 * @author hua
 * @date 2025/10/30
 */
public class ZSetStore {
    private static final Map<String, ZSetEntry> Z_SET = new ConcurrentHashMap<>();

    /**
     * 插入ZSet
     * @param key 键
     * @param set 值
     * @param map 值对应的分数
     */
    public static void set(String key, ConcurrentSkipListSet<ZSetValue> set, Map<String, Double> map) {
        if (getType(key) != null && !getType(key).equals(keyType.ZSET.toString()))
            throw new CacheForgeCodecException("类型不匹配");
        Z_SET.put(key, new ZSetEntry(set, map));
        setType(key, keyType.ZSET.toString());
    }

    /**
     * 获取ZSet
     * @param key 键
     * @return ZSet
     */
    public static ConcurrentSkipListSet<ZSetValue> get(String key) {
        if (Z_SET.get(key) == null) {
            return null;
        }
        if (!getType(key).equals(keyType.ZSET.toString())) throw new CacheForgeCodecException("类型不匹配");
        return (ConcurrentSkipListSet<ZSetValue>) Z_SET.get(key).getValues();
    }

    /**
     * 获取ZSet的成员和分数的映射
     * @param key 键
     * @return 映射
     */
    public static Map<String, Double> getMemberToScoreMap(String key) {
        if (Z_SET.get(key) == null) {
            return null;
        }
        if (!getType(key).equals(keyType.ZSET.toString())) throw new CacheForgeCodecException("类型不匹配");
        return Z_SET.get(key).getMemberToScore();
    }

}
