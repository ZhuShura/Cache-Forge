package fun.redis.cacheforge.protocol.model.header;

import fun.redis.cacheforge.common.CacheForgeCodecException;
import fun.redis.cacheforge.common.CacheForgeConstants;
import fun.redis.cacheforge.protocol.model.Message;
import io.netty.util.internal.StringUtil;

/**
 * 数组头消息
 * @author huangtaiji
 * @date 2025/10/26
 */
public class ArrayHeaderMessage implements Message {
	private final long length;

	public ArrayHeaderMessage(long length) {
		if (length < CacheForgeConstants.NULL_VALUE) {
			throw new CacheForgeCodecException("-ERROR length: " + length + ", expected length >= " + CacheForgeConstants.NULL_VALUE);
		}
		this.length = length;
	}

	public long length() {
		return length;
	}

	public boolean isNull() {
		return length == CacheForgeConstants.NULL_VALUE;
	}

	@Override
	public String toString() {
		return new StringBuilder(StringUtil.simpleClassName(this))
				.append('[')
				.append("length=")
				.append(length)
				.append(']').toString();
	}
}
