package fun.redis.cacheforge.storage.repo;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 集合存储仓库
 *
 * @author hua
 * @date 2025/10/26
 */
public class SetStoreZero {
    private static final Map<String, Set<String>> SET_MAP = new ConcurrentHashMap<>();

    /**
     * 添加元素
     *
     * @param key     键
     * @param values  值
     * @return 成功添加元素个数
     */
    public static Integer sAdd(String key, Set<String> values) {
        SET_MAP.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        Set<String> set = SET_MAP.get(key);
        int count = 0;
        for (String value : values) {
            if (value != null && set.add(value)) {
                count++;
            }
        }
        SET_MAP.put(key, set);
        return count;
    }

    /**
     * 删除元素
     *
     * @param key     键
     * @param values  值
     * @return 删除元素个数
     */
    public static Integer sRem(String key, Set<String> values) {
        Set<String> set = checkKey(key);
        int count = 0;
        for (String value : values) {
            if (value != null && set.remove(value)) {
                count++;
            }
        }
        SET_MAP.put(key, set);
        return count;
    }

    /**
     * 获取集合中所有元素
     *
     * @param key 键
     * @return 集合
     */
    public static Set<String> sMembers(String key) {
        return SET_MAP.get(key);
    }

    /**
     * 获取集合元素个数
     *
     * @param key 键
     * @return 元素个数
     */
    public static Integer sCard(String key) {
        return SET_MAP.get(key) == null ? 0 : SET_MAP.get(key).size();
    }

    /**
     * 判断元素是否在集合中
     *
     * @param key     键
     * @param member  元素
     * @return 是否存在
     */
    public static Boolean sIsMember(String key, String member) {
        Set<String> set = checkKey(key);
        return set.contains(member);
    }

    /**
     * 随机获取集合元素
     *
     * @param key     键
     * @param count   获取个数
     * @return 集合
     */
    public static Set<String> sRandMemBer(String key, int count) {
        return checkKey(key).stream()
                .limit(count)
                .collect(Collectors.toSet());
    }

    /**
     * 获取第一个集合与多个集合的差集
     *
     * @param keys 键
     * @return 集合
     */
    public static Set<String> sDiff(List<String> keys) {
        Set<String> set = checkKey(keys.get(0));
        for (int i = 1; i < keys.size(); i++) {
            Set<String> setI = checkKey(keys.get(i));
            set.removeAll(setI);
        }
        return set;
    }

    /**
     * 获取多个集合的差集，并保存到目标集合中
     *
     * @param destKey 目标键
     * @param keys    键
     * @return 集合
     */
    public static Integer sDiffStore(String destKey, List<String> keys) {
        Set<String> set = sDiff(keys);
        SET_MAP.put(destKey, set);
        return set.size();
    }

    /**
     * 获取第一个集合与多个集合的交集
     *
     * @param keys 键
     * @return 集合
     */
    public static Set<String> sInter(List<String> keys) {
        Set<String> set = new HashSet<>();
        for (String key : keys) {
           Set<String> curSet = checkKey(key);
           if (set.isEmpty())
               set = new HashSet<>(curSet);
           else {
               set.retainAll(curSet);
               if (set.isEmpty())
                   break;
           }
        }
        return set;
    }

    /**
     * 获取多个集合的交集，并保存到目标集合中
     *
     * @param destKey 目标键
     * @param keys    键
     * @return 集合
     */
    public static Integer sInterStore(String destKey, List<String> keys) {
        Set<String> set = sInter(keys);
        SET_MAP.put(destKey, set);
        return set.size();
    }

    /**
     * 获取多个集合的并集
     *
     * @param keys 键
     * @return 集合
     */
    public static Set<String> sUnion(List<String> keys) {
        Set<String> set = new HashSet<>();
        for (String key : keys) {
            Set<String> curSet = checkKey(key);
            set.addAll(curSet);
        }
        return set;
    }

    /**
     * 获取多个集合的并集，并保存到目标集合中
     *
     * @param destKey 目标键
     * @param keys    键
     * @return 集合中元素数量
     */
    public static Integer sUnionStore(String destKey, List<String> keys) {
        Set<String> set = sUnion(keys);
        SET_MAP.put(destKey, set);
        return set.size();
    }

    /**
     * 移动元素
     *
     * @param source      源键
     * @param destination 目标键
     * @param member      元素
     * @return 是否成功
     */
    public static Boolean sMove(String source, String destination, String member) {
        Set<String> sourceSet = checkKey(source);
        Set<String> destinationSet = checkKey(destination);
        if (sourceSet.remove(member)) {
            destinationSet.add(member);
            SET_MAP.put(source, sourceSet);
            SET_MAP.put(destination, destinationSet);
            return true;
        }
        return false;
    }

    /**
     * 随机获取集合元素并删除
     *
     * @param key     键
     * @param count   获取个数
     * @return 集合
     */
    public static Set<String> sPop(String key, int count) {
        return checkKey(key).stream()
                .limit(count)
                .collect(Collectors.toSet());
    }

    /**
     * 检查key是否可用
     *
     * @param key 键
     * @return 集合
     */
    private static Set<String> checkKey(String key) {
        Set<String> set = SET_MAP.get(key);
        if (set == null) throw new NullPointerException("key not exists");
        return set;
    }
}
