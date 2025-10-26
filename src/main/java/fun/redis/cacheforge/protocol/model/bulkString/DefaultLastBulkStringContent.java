package fun.redis.cacheforge.protocol.model.bulkString;

import io.netty.buffer.ByteBuf;

/**
 * 不完整的最后一个bulk string内容
 * @author huangtaiji
 * @date 2025/10/26
 */
public class DefaultLastBulkStringContent extends DefaultBulkStringContent implements LastBulkStringContent {
	public DefaultLastBulkStringContent (ByteBuf content) {
		super(content);
	}
}
