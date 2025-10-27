package fun.redis.cacheforge.protocol.model.bulkString;

import fun.redis.cacheforge.protocol.model.Message;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.ByteBuf;

/**
 * 批量字符串内容接口
 * @author huangtaiji
 * @date 2025/10/26
 */
public interface BulkStringContent extends Message, ByteBufHolder {
	@Override
	BulkStringContent copy();

	@Override
	BulkStringContent duplicate();

	@Override
	BulkStringContent retainedDuplicate();

	@Override
	BulkStringContent replace(ByteBuf content);

	@Override
	BulkStringContent retain();

	@Override
	BulkStringContent retain(int increment);

	@Override
	BulkStringContent touch();

	@Override
	BulkStringContent touch(Object hint);
}
