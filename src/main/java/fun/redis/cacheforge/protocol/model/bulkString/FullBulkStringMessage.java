package fun.redis.cacheforge.protocol.model.bulkString;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.StringUtil;

/**
 * 完整字符串消息
 * @author huangtaiji
 * @date 2025/10/26
 */
public class FullBulkStringMessage extends DefaultByteBufHolder implements LastBulkStringContent {


	/**
	 * 空字符串消息
	 */
	public static final FullBulkStringMessage EMPTY_INSTANCE = new FullBulkStringMessage(){
		@Override
		public ByteBuf content() {
			return Unpooled.EMPTY_BUFFER;
		}

		@Override
		public FullBulkStringMessage copy() {
			return this;
		}

		@Override
		public FullBulkStringMessage duplicate() {
			return this;
		}

		@Override
		public FullBulkStringMessage retainedDuplicate() {
			return this;
		}

		@Override
		public int refCnt() {
			return 1;
		}

		@Override
		public FullBulkStringMessage retain() {
			return this;
		}

		@Override
		public FullBulkStringMessage retain(int increment) {
			return this;
		}

		@Override
		public FullBulkStringMessage touch() {
			return this;
		}

		@Override
		public FullBulkStringMessage touch(Object hint) {
			return this;
		}

		@Override
		public boolean release() {
			return false;
		}

		@Override
		public boolean release(int decrement) {
			return false;
		}
	};

	/**
	 * nil字符串消息
	 */
	public static final FullBulkStringMessage NULL_INSTANCE = new FullBulkStringMessage(){
		@Override
		public ByteBuf content() {
			return Unpooled.EMPTY_BUFFER;
		}

		@Override
		public boolean isNull() {
			return true;
		}

		@Override
		public FullBulkStringMessage copy() {
			return this;
		}

		@Override
		public FullBulkStringMessage duplicate() {
			return this;
		}

		@Override
		public FullBulkStringMessage retainedDuplicate() {
			return this;
		}

		@Override
		public int refCnt() {
			return 1;
		}

		@Override
		public FullBulkStringMessage retain() {
			return this;
		}

		@Override
		public FullBulkStringMessage retain(int increment) {
			return this;
		}

		@Override
		public FullBulkStringMessage touch() {
			return this;
		}

		@Override
		public FullBulkStringMessage touch(Object hint) {
			return this;
		}

		@Override
		public boolean release() {
			return false;
		}

		@Override
		public boolean release(int decrement) {
			return false;
		}
	};


	private FullBulkStringMessage() {
		this(Unpooled.EMPTY_BUFFER);
	}

	public FullBulkStringMessage(ByteBuf content) {
		super(content);
	}

	public boolean isNull() {
		return false;
	}

	@Override
	public FullBulkStringMessage copy() {
		return (FullBulkStringMessage) super.copy();
	}

	@Override
	public FullBulkStringMessage duplicate() {
		return (FullBulkStringMessage) super.duplicate();
	}

	@Override
	public FullBulkStringMessage retainedDuplicate() {
		return (FullBulkStringMessage) super.retainedDuplicate();
	}

	@Override
	public FullBulkStringMessage replace(ByteBuf content) {
		return new FullBulkStringMessage(content);
	}

	@Override
	public FullBulkStringMessage retain() {
		super.retain();
		return this;
	}

	@Override
	public FullBulkStringMessage retain(int increment) {
		super.retain(increment);
		return this;
	}

	@Override
	public FullBulkStringMessage touch() {
		super.touch();
		return this;
	}

	@Override
	public FullBulkStringMessage touch(Object hint) {
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
