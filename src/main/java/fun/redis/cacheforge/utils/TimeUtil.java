package fun.redis.cacheforge.utils;

import fun.redis.cacheforge.storage.repo.GlobalStore;

public class TimeUtil {

	public static final boolean isExpire(String key) {
		Long expire = GlobalStore.getExpire(key);
		if (expire == null) {
			return false;
		}
		return expire < System.currentTimeMillis();
	}
}
