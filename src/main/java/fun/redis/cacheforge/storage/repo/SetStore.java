package fun.redis.cacheforge.storage.repo;

import fun.redis.cacheforge.common.CacheForgeCodecException;
import fun.redis.cacheforge.storage.model.SetEntry;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static fun.redis.cacheforge.storage.repo.GlobalStore.*;

/**
 * 集合存储仓库
 * @author hua
 * @date 2025/10/29
 */
public class SetStore {
    private static final Map<String, SetEntry> SET_MAP = new ConcurrentHashMap<>();

    /**
     * 设置集合
     * @param key 键
     * @param set 集合
     */
    public static void set(String key, Set<String> set) {
        SET_MAP.put(key, new SetEntry(set));
        setType(key, keyType.SET.toString());
    }

    /**
     * 获取集合
     * @param key 键
     * @return 集合
     */
    public static Set<String> get(String key) throws CacheForgeCodecException {
       if (SET_MAP.get(key) == null) {
           return null;
       }
       if (!getType(key).equals(keyType.SET.toString())) throw new CacheForgeCodecException("类型不匹配");
       return SET_MAP.get(key).getValue();
    }

}
