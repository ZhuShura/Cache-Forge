package fun.redis.cacheforge.protocol;

import fun.redis.cacheforge.common.CacheForgeCodecException;
import fun.redis.cacheforge.protocol.model.Message;
import fun.redis.cacheforge.protocol.model.array.ArrayHeaderMessage;
import fun.redis.cacheforge.protocol.model.array.ArrayMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 数组聚合器
 * @author huangtaiji
 * @date 2025/10/26
 */
public final class ArrayAggregator extends MessageToMessageDecoder<Message> {

	/**
	 * 支持接收嵌套数组，最深4层
	 */
	private final Deque<AggregatorState> depths = new ArrayDeque<>(4);

	@Override
	protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
		if (msg instanceof ArrayHeaderMessage) {
			msg = decodeArrayHeader((ArrayHeaderMessage) msg);
			if (msg == null) {
				return;
			}
		} else {
			ReferenceCountUtil.retain(msg);
		}

		while (!depths.isEmpty()) {
			AggregatorState current = depths.peek();
			current.children.add(msg);

			if (current.children.size() == current.length) {
				msg = new ArrayMessage(current.children);
				depths.pop();
			} else {
				return;
			}
		}

		out.add(msg);
	}



	/**
	 * 解析数组头
	 * @param header 数组头
	 * @return 解析后的消息
	 */
	private Message decodeArrayHeader (ArrayHeaderMessage header) {
		if (header.isNull()) {
			return ArrayMessage.NULL_INSTANCE;
		} else if (header.length() == 0L) {
			return ArrayMessage.EMPTY_INSTANCE;
		} else if (header.length() > 0L) {
			if (header.length() > Integer.MAX_VALUE) {
				throw new CacheForgeCodecException("-ERROR doesn't support longer length than " + Integer.MAX_VALUE);
			}
			depths.push(new AggregatorState((int) header.length()));
			return null;
		} else {
			throw new CacheForgeCodecException("-ERROR bad array length: " + header.length());
		}
	}



	/**
	 * 聚合状态
	 */
	private static final class AggregatorState {
		private final int length;
		private final List<Message> children;

		AggregatorState(int length) {
			this.length = length;  // 目标长度（数组header）
			this.children = new ArrayList<Message>(length);
		}
	}
}
