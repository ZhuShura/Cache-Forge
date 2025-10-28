package fun.redis.cacheforge.storage.model;

import lombok.Data;

/**
 * CacheForge字符串实体
 * @author huangtaiji
 * @date 2025/10/27
 */
@Data
public class StringEntry {
	private String value;       // 值
	private Long expireTime;    // 过期时间戳

	public StringEntry(String value) {
		this.value = value;
		this.expireTime = null;
	}

	public StringEntry(String value, Long expireTime) {
		this.value = value;
		this.expireTime = expireTime;
	}

	/**
	 * 判断是否过期
	 * @return 是否过期
	 */
	public boolean isExpired() {
		return expireTime != null && expireTime < System.currentTimeMillis();
	}

	/**
	 * 获取字符串长度
	 * @return 字符串长度
	 */
	public int length() {
		return value == null ? 0 : value.length();
	}
}
