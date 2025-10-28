package fun.redis.cacheforge.storage.repo;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局存储仓库
 * @author huangtaiji
 * @date 2025/10/28
 */
public class GlobalStore {
	private static final Map<String, String> TYPE_MAP = new ConcurrentHashMap<>();
	private static final Map<String, Long> EXPIRE_MAP = new ConcurrentHashMap<>();

	/**
	 * 设置类型
	 * @param key 键
	 * @param type 类型
	 */
	public static void setType(String key, String type) {
		TYPE_MAP.put(key, type);
	}

	/**
	 * 获取类型
	 * @param key 键
	 * @return 类型
	 */
	public static String getType(String key) {
		return TYPE_MAP.get(key);
	}

	/**
	 * 设置过期时间
	 * @param key 键
	 * @param expire 过期时间
	 */
	public static void setExpire(String key, Long expire) {
		EXPIRE_MAP.put(key, expire + System.currentTimeMillis());
	}

	/**
	 * 获取过期时间
	 * @param key 键
	 * @return 过期时间
	 */
	public static Long getExpire(String key) {
		return EXPIRE_MAP.get(key);
	}

	/**
	 * 删除过期时间
	 * @param key 键
	 */
	public static void delExpire(String key) {
		EXPIRE_MAP.remove(key);
	}
}
