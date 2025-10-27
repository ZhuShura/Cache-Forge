package fun.redis.cacheforge.protocol.model.bulkString;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;
import io.netty.util.internal.StringUtil;

/**
 * 非完整的bulk string内容实现类
 * @author huangtaiji
 * @date 2025/10/26
 */
public class DefaultBulkStringContent extends DefaultByteBufHolder implements BulkStringContent {
	public DefaultBulkStringContent(ByteBuf content) {
		super(content);
	}

	@Override
	public BulkStringContent copy() {
		return (BulkStringContent) super.copy();
	}

	@Override
	public BulkStringContent duplicate() {
		return (BulkStringContent) super.duplicate();
	}

	@Override
	public BulkStringContent retainedDuplicate() {
		return (BulkStringContent) super.retainedDuplicate();
	}

	@Override
	public BulkStringContent replace(ByteBuf content) {
		return new DefaultBulkStringContent(content);
	}

	@Override
	public BulkStringContent retain() {
		super.retain();
		return this;
	}

	@Override
	public BulkStringContent retain(int increment) {
		super.retain(increment);
		return this;
	}

	@Override
	public BulkStringContent touch() {
		super.touch();
		return this;
	}

	@Override
	public BulkStringContent touch(Object hint) {
		super.touch(hint);
		return this;
	}

	@Override
	public String toString() {
		return new StringBuilder(StringUtil.simpleClassName(this))
				.append('[')
				.append("content=")
				.append(content())
				.append(']').toString();
	}
}
