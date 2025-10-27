package fun.redis.cacheforge.protocol.model.bulkString;

import fun.redis.cacheforge.common.CacheForgeCodecException;
import fun.redis.cacheforge.common.CacheForgeConstants;
import fun.redis.cacheforge.protocol.model.Message;

/**
 * 批量字符串消息头
 * @author huangtaiji
 * @date 2025/10/26
 */
public class BulkStringHeaderMessage implements Message {

	private final long length;

	public BulkStringHeaderMessage(long length) {
		if (length <= 0) {
			throw new CacheForgeCodecException("-ERROR length: " + length + ", expected length > 0");
		}
		this.length = length;
	}

	public long length() {
		return length;
	}

	public boolean isNull() {
		return length == CacheForgeConstants.NULL_VALUE;
	}
}
