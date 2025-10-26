package fun.redis.cacheforge.protocol.model.bulkString;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;

/**
 * 非完整的bulk string内容
 * @author huangtaiji
 * @date 2025/10/26
 */
public class DefaultBulkStringContent extends DefaultByteBufHolder implements BulkStringContent {
	public DefaultBulkStringContent(ByteBuf content) {
		super(content);
	}
}
