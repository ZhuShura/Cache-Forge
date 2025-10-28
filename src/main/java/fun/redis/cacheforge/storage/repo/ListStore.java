package fun.redis.cacheforge.storage.repo;

import fun.redis.cacheforge.storage.model.ListEntry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 列表存储仓库
 *
 * @author huangtaiji
 * @date 2025/10/27
 */
public class ListStore {
    private static final Map<String, ListEntry> LIST_MAP = new ConcurrentHashMap<>();

    /**
     * 插入列表
     * @param key 键
     * @param values 值
     */
    public static void set(String key, List<String> values) {
        LIST_MAP.put(key, new ListEntry(values));
    }

    /**
     * 获取列表
     * @param key 键
     * @return 列表
     */
    public static List<String> get(String key) {
        if (LIST_MAP.get(key) == null) {
            return null;
        }
        return LIST_MAP.get(key).getValue();
    }

}
