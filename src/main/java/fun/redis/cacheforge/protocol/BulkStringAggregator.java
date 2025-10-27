package fun.redis.cacheforge.protocol;

import fun.redis.cacheforge.common.CacheForgeConstants;
import fun.redis.cacheforge.protocol.model.Message;
import fun.redis.cacheforge.protocol.model.bulkString.BulkStringContent;
import fun.redis.cacheforge.protocol.model.bulkString.BulkStringHeaderMessage;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.protocol.model.bulkString.LastBulkStringContent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageAggregator;

public final class BulkStringAggregator extends MessageAggregator<Message, BulkStringHeaderMessage,
		BulkStringContent, FullBulkStringMessage> {
	public BulkStringAggregator() {
		super(CacheForgeConstants.MESSAGE_MAX_LENGTH);
	}

	@Override
	public boolean isStartMessage(Message msg) throws Exception{
		return msg instanceof BulkStringHeaderMessage;
	}

	@Override
	public boolean isContentMessage(Message msg) throws Exception{
		return msg instanceof BulkStringContent;
	}

	@Override
	public boolean isLastContentMessage(BulkStringContent msg) throws Exception{
		return msg instanceof LastBulkStringContent;
	}

	@Override
	public boolean isAggregated(Message msg) throws Exception{
		return msg instanceof FullBulkStringMessage;
	}

	@Override
	protected boolean isContentLengthInvalid(BulkStringHeaderMessage start, int maxContentLength) throws Exception {
		return start.length() > maxContentLength;
	}

	@Override
	protected Object newContinueResponse(BulkStringHeaderMessage start, int maxContentLength, ChannelPipeline pipeline) throws Exception {
		return null;
	}

	@Override
	protected boolean closeAfterContinueResponse(Object msg) throws Exception {
		throw new UnsupportedOperationException("This method should not be called");
	}

	@Override
	protected boolean ignoreContentAfterContinueResponse(Object msg) throws Exception {
		throw new UnsupportedOperationException("This method should not be called");
	}

	@Override
	protected FullBulkStringMessage beginAggregation(BulkStringHeaderMessage start, ByteBuf content) throws Exception {
		return new FullBulkStringMessage(content);
	}


}
