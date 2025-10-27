package fun.redis.cacheforge.protocol.model.bulkString;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 最后一个bulk string内容
 *
 * @author huangtaiji
 * @date 2025/10/26
 */
public interface LastBulkStringContent extends BulkStringContent {

	/**
	 * 最后一个bulk string内容
	 */
	LastBulkStringContent EMPTY_LAST_CONTENT = new LastBulkStringContent() {
		@Override
		public ByteBuf content() {
			return Unpooled.EMPTY_BUFFER;
		}

		@Override
		public LastBulkStringContent copy() {
			return this;
		}

		@Override
		public LastBulkStringContent duplicate() {
			return this;
		}

		@Override
		public LastBulkStringContent retainedDuplicate() {
			return this;
		}

		@Override
		public LastBulkStringContent replace(ByteBuf content) {
			return new DefaultLastBulkStringContent(content);
		}

		@Override
		public LastBulkStringContent retain() {
			return this;
		}

		@Override
		public LastBulkStringContent retain(int increment) {
			return this;
		}

		@Override
		public LastBulkStringContent touch() {
			return this;
		}

		@Override
		public LastBulkStringContent touch(Object hint) {
			return this;
		}

		@Override
		public int refCnt() {
			return 1;
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


	@Override
	public LastBulkStringContent copy();

	@Override
	public LastBulkStringContent duplicate();

	@Override
	public LastBulkStringContent retainedDuplicate();

	@Override
	public LastBulkStringContent replace(ByteBuf content);

	@Override
	public LastBulkStringContent retain();

	@Override
	public LastBulkStringContent retain(int increment);

	@Override
	public LastBulkStringContent touch();

	@Override
	public LastBulkStringContent touch(Object hint);

}
