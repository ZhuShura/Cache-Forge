package fun.redis.cacheforge.protocol.model.bulkString;

import io.netty.buffer.ByteBuf;

/**
 * 不完整的最后一个bulk string内容实现类
 * @author huangtaiji
 * @date 2025/10/26
 */
public final class DefaultLastBulkStringContent extends DefaultBulkStringContent implements LastBulkStringContent {
	public DefaultLastBulkStringContent (ByteBuf content) {
		super(content);
	}

	@Override
	public LastBulkStringContent copy() {
		return (LastBulkStringContent) super.copy();
	}

	@Override
	public LastBulkStringContent duplicate() {
		return (LastBulkStringContent) super.duplicate();
	}

	@Override
	public LastBulkStringContent retainedDuplicate() {
		return (LastBulkStringContent) super.retainedDuplicate();
	}

	@Override
	public LastBulkStringContent replace(ByteBuf content) {
		return new DefaultLastBulkStringContent(content);
	}

	@Override
	public LastBulkStringContent retain() {
		super.retain();
		return this;
	}

	@Override
	public LastBulkStringContent retain(int increment) {
		super.retain(increment);
		return this;
	}

	@Override
	public LastBulkStringContent touch() {
		super.touch();
		return this;
	}

	@Override
	public LastBulkStringContent touch(Object hint) {
		super.touch(hint);
		return this;
	}

}
