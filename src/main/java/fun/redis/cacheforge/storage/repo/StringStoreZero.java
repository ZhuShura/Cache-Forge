package fun.redis.cacheforge.storage.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字符串存储仓库
 * @author hua
 * @date 2025/10/26
 */
public class StringStoreZero {
    private static final String NIL = "nil";
    private static final String INT = "int";
    private static final Integer CHANGE_NUM = 1;
    private static final Map<String, String> STRING_MAP = new ConcurrentHashMap<>();
    private static final Map<String, String> TYPE_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Long> EXPIRE_TIMES = new ConcurrentHashMap<>();

    /**
     * 插入字符串
     * @param key 键
     * @param value 值
     */
    public static void set(String key, String value) {
        STRING_MAP.put(key, value);
    }

    /**
     * 获取字符串
     * @param key 键
     * @return 值
     */
    public static String get(String key) {
        if (EXPIRE_TIMES.get(key) != null && EXPIRE_TIMES.get(key) < System.currentTimeMillis()) {
            STRING_MAP.remove(key);
            EXPIRE_TIMES.remove(key);
            return NIL;
        }
        return STRING_MAP.get(key) == null ? NIL : STRING_MAP.get(key);
    }

    /**
     * 删除字符串
     * @param key 键
     * @return 是否删除成功
     */
    public static Boolean del(String key) {
        return STRING_MAP.remove(key, STRING_MAP.get(key));
    }

    /**
     * 批量插入字符串
     * @param keysAndValues 键值对
     */
    public static void mSet(Map<String, String> keysAndValues) {
       STRING_MAP.putAll(keysAndValues);
    }

    /**
     * 批量获取字符串
     * @param keys 键
     * @return 值
     */
    public static List<String> mGet(List<String> keys) {
          List<String> values = new ArrayList<>();
          for (String key : keys) {
              if (EXPIRE_TIMES.get(key) != null && EXPIRE_TIMES.get(key) < System.currentTimeMillis()) {
                  STRING_MAP.remove(key);
                  EXPIRE_TIMES.remove(key);
                  values.add(NIL);
              }
              values.add(STRING_MAP.get(key) == null ? NIL : STRING_MAP.get(key));
          }
          return values;
    }

    /**
     * key对应的value数值增加1并记录为整数类型
     * @param key 键
     * @return value值
     */
    public static Integer incr(String key) {
        if (STRING_MAP.get(key) == null) {
            STRING_MAP.put(key, "1");
            TYPE_MAP.put(key, INT);
            return Integer.parseInt(STRING_MAP.get(key));
        }
        STRING_MAP.put(key, String.valueOf(Integer.parseInt(STRING_MAP.get(key)) + CHANGE_NUM));
        return Integer.parseInt(STRING_MAP.get(key));
    }

    /**
     * key对应的value数值减少1并记录为整数类型
     * @param key 键
     * @return value值
     */
    public static Integer decr(String key) {
        if (STRING_MAP.get(key) == null) {
            STRING_MAP.put(key, "-1");
            TYPE_MAP.put(key, INT);
            return Integer.parseInt(STRING_MAP.get(key));
        }
        STRING_MAP.put(key, String.valueOf(Integer.parseInt(STRING_MAP.get(key)) - CHANGE_NUM));
        return Integer.parseInt(STRING_MAP.get(key));
    }

    /**
     * key对应的value数值增加指定值并记录为整数类型
     * @param key 键
     * @param decrement 增加的值
     * @return value值
     */
    public static Integer incrBy(String key, Integer decrement) {
        if (STRING_MAP.get(key) == null) {
            STRING_MAP.put(key, String.valueOf(decrement));
            TYPE_MAP.put(key, INT);
            return Integer.parseInt(STRING_MAP.get(key));
        }
        STRING_MAP.put(key, String.valueOf(Integer.parseInt(STRING_MAP.get(key)) + decrement));
        return Integer.parseInt(STRING_MAP.get(key));
    }

    /**
     * key对应的value数值减少指定值并记录为整数类型
     * @param key 键
     * @param decrement 减少的值
     * @return value值
     */
    public static Integer decrBy(String key, Integer decrement) {
        if (STRING_MAP.get(key) == null) {
            STRING_MAP.put(key, String.valueOf(-decrement));
            TYPE_MAP.put(key, INT);
            return Integer.parseInt(STRING_MAP.get(key));
        }
        STRING_MAP.put(key, String.valueOf(Integer.parseInt(STRING_MAP.get(key)) - decrement));
        return Integer.parseInt(STRING_MAP.get(key));
    }

    /**
     * 追加字符串
     * @param key 键
     * @param value 值
     * @return 追加后的长度
     */
    public static Integer append(String key, String value) {
        String v = STRING_MAP.get(key);
        v = v + value;
        STRING_MAP.put(key, v);
        return v.length();
    }

    /**
     * 获取字符串长度
     * @param key 键
     * @return 字符串长度
     */
    public static Integer strlen(String key) {
        return STRING_MAP.get(key) == null ? 0 : STRING_MAP.get(key).length();
    }

    /**
     * 获取字符串指定区间
     * @param key 键
     * @param start 开始位置
     * @param end 结束位置
     * @return 指定区间
     */
    public static String getRange(String key, Integer start, Integer end) {
        return STRING_MAP.get(key) == null ? NIL : STRING_MAP.get(key).substring(start, end);
    }

    /**
     * 修改字符串指定区间
     * @param key 键
     * @param offset 索引位置
     * @param value 值
     * @return 修改后的值
     */
    public static String setRange(String key, Integer offset, String value) {
        String v = STRING_MAP.get(key);
        v = v.substring(0, offset) + value;
        STRING_MAP.put(key, v);
        return v;
    }

    /**
     * 设置字符串，如果key已经存在，则返回false
     * @param key 键
     * @param value 值
     * @return 是否设置成功
     */
    public static Boolean setNx(String key, String value) {
        return STRING_MAP.putIfAbsent(key, value) == null;
    }

    /**
     * 设置字符串，并指定过期时间
     * @param key 键
     * @param seconds 过期时间
     * @param value 值
     */
    public static void setEx(String key, Integer seconds, String value) {
        STRING_MAP.put(key, value);
        EXPIRE_TIMES.put(key, System.currentTimeMillis() + seconds * 1000);
    }

    /**
     * 设置字符串，并指定过期时间
     * @param key 键
     * @param milliseconds 过期时间
     * @param value 值
     */
    public static void psetEx(String key, Integer milliseconds, String value) {
        STRING_MAP.put(key, value);
        EXPIRE_TIMES.put(key, System.currentTimeMillis() + milliseconds);
    }

    /**
     * 获取并设置字符串
     * @param key 键
     * @param value 值
     * @return 获取的值
     */
    public static String getSet(String key, String value) {
        String s = STRING_MAP.get(key);
        STRING_MAP.put(key, value);
        return s == null ? NIL : s;
    }

}
