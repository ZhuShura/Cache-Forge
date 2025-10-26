package fun.redis.cacheforge.storage.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 列表存储仓库
 *
 * @author hua
 * @date 2025/10/26
 */
public class ListStore {
    private static final String NIL = "nil";
    private static final Map<String, List<String>> LIST_MAP = new ConcurrentHashMap<>();

    /**
     * 左侧压栈
     *
     * @param key   键
     * @param values 值
     * @return 列表长度
     */
    public static Integer lPush(String key, List<String> values) {
        LIST_MAP.computeIfAbsent(key, k -> new ArrayList<>());
        List<String> list = LIST_MAP.get(key);
        list.addAll(0, values);
        LIST_MAP.put(key, list);
        return list.size();
    }

    /**
     * 右侧压栈
     *
     * @param key   键
     * @param values 值
     * @return 列表长度
     */
    public static Integer rPush(String key, List<String> values) {
        LIST_MAP.computeIfAbsent(key, k -> new ArrayList<>());
        List<String> list = LIST_MAP.get(key);
        list.addAll(values);
        LIST_MAP.put(key, list);
        return list.size();
    }

    /**
     * 左侧弹出
     *
     * @param key 键
     * @param count 数量
     * @return 被移除的元素
     */
    public static List<String> lPop(String key, Integer count) {
        List<String> list = checkKey(key);
        List<String> removed = new ArrayList<>();
        while (count -- > 0 && !list.isEmpty()) {
            removed.add(list.removeFirst());
        }
        LIST_MAP.put(key, list);
        return removed;
    }

    /**
     * 右侧弹出
     *
     * @param key 键
     * @param count 数量
     * @return 被移除的元素
     */
    public static List<String> rPop(String key, Integer count) {
        List<String> list = checkKey(key);
        List<String> removed = new ArrayList<>();
        while (count -- > 0 && !list.isEmpty()) {
            removed.add(list.removeLast());
        }
        LIST_MAP.put(key, list);
        return removed;
    }

    /**
     * 获取列表区间元素
     *
     * @param key 键
     * @param start 开始索引
     * @param end 结束索引
     * @return 列表区间元素
     */
    public static List<String> lRange(String key, Integer start, Integer end) {
        List<String> list = checkKey(key);
        start = start < 0 ? list.size() + start : start;
        end = end < 0 ? list.size() + end : end;
        return list.subList(start, end);
    }

    /**
     * 获取列表长度
     *
     * @param key 键
     * @return 列表长度
     */
    public static Integer lLen(String key) {
        return LIST_MAP.get(key) == null ? 0 : LIST_MAP.get(key).size();
    }

    /**
     * 获取列表指定索引元素
     *
     * @param key 键
     * @param index 索引
     * @return 列表指定索引元素
     */
    public static String lIndex(String key, Integer index) {
        List<String> list = checkKey(key);
        index = index < 0 ? list.size() + index : index;
        return list.get(index).isEmpty() ? list.get(index) : NIL;
    }

    /**
     * 修改列表指定索引元素
     *
     * @param key 键
     * @param index 索引
     * @param value 值
     */
    public static void lSet(String key, Integer index, String value) {
        List<String> list = checkKey(key);
        index = index < 0 ? list.size() + index : index;
        list.set(index, value);
        LIST_MAP.put(key, list);
    }

    /**
     * 在列表指定元素前后插入元素
     *
     * @param key 键
     * @param flag 插入位置
     * @param pivot 插入位置元素
     * @param value 插入元素
     * @return 列表长度
     */
    public static Integer lInsert(String key, String flag, String pivot, String value) {
        List<String> list = checkKey(key);
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(pivot)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return index;
        }
        if ("BEFORE".equals(flag)) {
            list.add(index, value);
        } else if ("AFTER".equals(flag)) {
            list.add(index + 1, value);
        }
        LIST_MAP.put(key, list);
        return list.size();
    }

    /**
     * 截取列表
     *
     * @param key 键
     * @param start 索引
     * @param end 索引
     */
    public static void lTrim(String key, Integer start, Integer end) {
        List<String> list = checkKey(key);
        list = list.stream()
                .skip(start)
                .limit(end - start + 1)
                .toList();
        LIST_MAP.put(key, list);
    }

    /**
     * 移除并返回列表最后一个元素，并将该元素添加到另一个列表
     *
     * @param source 源列表
     * @param destination 目标列表
     * @return 被操作的元素
     */
    public static String rPopLPush(String source, String destination) {
        List<String> rList = checkKey(source);
        List<String> lList = checkKey(destination);
        lList.add(0, rList.removeLast());
        LIST_MAP.put(source, rList);
        LIST_MAP.put(destination, lList);
        return rList.getLast();
    }

    /**
     * 移除列表中与value相同的元素
     *
     * @param key 键
     * @param count 移除数量
     * @param value 值
     * @return 移除数量
     */
    public static Integer lRem(String key, Integer count, String value) {
        List<String> list = checkKey(key);
        int tmp = 0;
        if (count == 0) {
            for (String s : list) {
                if (s.equals(value)) {
                    list.remove(s);
                    tmp++;
                }
            }
        } else if (count > 0) {
            for (String s : list) {
                if (s.equals(value)) {
                    list.remove(s);
                    tmp++;
                    if (tmp == count) {
                        break;
                    }
                }
            }
        } else {
            for (int i = list.size() - 1; i >= 0 && tmp != -count; i--) {
                if (list.get(i).equals(value)) {
                    list.remove(i);
                    tmp++;
                }
            }
        }
        LIST_MAP.put(key, list);
        return tmp;
    }

    /**
     * 检查key是否可用
     *
     * @param key 键
     * @return 列表
     */
    private static List<String> checkKey(String key) {
        if (key == null || key.isEmpty() || LIST_MAP.get(key) == null) {
            throw new NullPointerException("key is null");
        }
        return LIST_MAP.get(key);
    }
}
