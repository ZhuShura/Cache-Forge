package fun.redis.cacheforge.common;

import fun.redis.cacheforge.utils.CacheForgeCodecUtil;
import lombok.Getter;

/**
 * 项目常量
 * @author huangtaiji
 * @date 2025/10/26
 */
public class CacheForgeConstants {
	private CacheForgeConstants(){}

	public static final int TYPE_LENGTH = 1;                                                        // 类型字节长度
	public static final int EOL_LENGTH = 2;                                                         // CRLF字节长度
	public static final short EOL_SHORT = CacheForgeCodecUtil.makeShort('\r', '\n');                // CRLF(short类型)
	public static final int NULL_VALUE = -1;                                                        // nil值
	public static final int NULL_LENGTH = 2;                                                        // nil字节长度
	public static final int NULL_SHORT = CacheForgeCodecUtil.makeShort('-', '1');                   // nil(short类型)
	public static final int INLINE_MESSAGE_MAX_LENGTH = 64 * 1024;                                  // 内联命令最大长度
	public static final int POSITIVE_LONG_MAX_LENGTH = 19;                                          // long正数的最大长度
	public static final int LONG_MAX_LENGTH = POSITIVE_LONG_MAX_LENGTH + 1;                         // long最大长度
	public static final int MESSAGE_MAX_LENGTH = 512 * 1024 * 1024;                                 // 命令单个参数最大长度 512MB

	/**
	 * 过期时间单位
	 */
	public enum ExpireUnit {
		EX("EX"),
		PX("PX");

		@Getter
		private final String name;
		private final long value;

		ExpireUnit(String name) {
			this.name = name;
			this.value = name.equals("EX") ? 1000L : 1L;
		}

		public long value() {
			return value;
		}
	}
}
