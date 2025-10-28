package fun.redis.cacheforge.storage.model;

import lombok.Data;

import java.time.Instant;

/**
 * CacheForge字符串实体
 * @author huangtaiji
 * @date 2025/10/27
 */
@Data
public class StringEntry {
	private String value;       // 值

	public StringEntry(String value) {
		this.value = value;
	}

	/**
	 * 获取字符串长度
	 * @return 字符串长度
	 */
	public int length() {
		return value == null ? 0 : value.length();
	}
}
