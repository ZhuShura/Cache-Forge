package fun.redis.cacheforge.storage.repo;

import fun.redis.cacheforge.storage.model.ZSetEntry;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 *zSet存储
 * @author hua
 * @date 2025/10/26
 */
public class ZSetStoreZero {
    /**
     * 默认按score升序排序, score相同时按member字典序排序
     */
    private static final Map<String, TreeSet<ZSetEntry>> Z_SET = new ConcurrentHashMap<>();


    /**
     * 添加元素
     * @param key 键
     * @param members 成员
     * @param scores 分数
     * @return 成功添加的元素个数
     */
    public static Integer zAdd(String key, List<String> members, List<Double> scores) {
        Z_SET.computeIfAbsent(key, k -> new TreeSet<>());
        TreeSet<ZSetEntry> zSet = Z_SET.get(key);
        int count = 0;
        for (int i = 0; i < members.size(); i++) {
            if (zSet.add(new ZSetEntry(members.get(i), scores.get(i)))) {
                count ++;
            }
        }
        Z_SET.put(key, zSet);
        return count;
    }

    /**
     * 删除成员
     * @param key 键
     * @param members 成员
     * @return 删除的元素个数
     */
    public static Integer zRem(String key, List<String> members) {
        TreeSet<ZSetEntry> zSet = checkKey(key);
        int count = 0;
        for (String member : members) {
            for (ZSetEntry entry : zSet) {
                if (entry.getMember().equals(member)) {
                    if (zSet.remove(entry)) {
                        count++;
                    }
                    break;
                }
            }
        }
        return count;
    }

    /**
     * 检查key是否可用
     * @param key 键
     * @return 集合
     */
    private static TreeSet<ZSetEntry> checkKey(String key) {
        TreeSet<ZSetEntry> zSet = Z_SET.get(key);
        if (zSet == null) throw new NullPointerException("key not exists");
        return zSet;
    }
}
