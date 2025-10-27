package fun.redis.cacheforge.protocol.model.pool;


import fun.redis.cacheforge.common.CacheForgeConstants;
import fun.redis.cacheforge.protocol.model.generalizedInline.ErrorMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.IntegerMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.SimpleStringMessage;
import fun.redis.cacheforge.utils.CacheForgeCodecUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;


/**
 * 固定消息池 ———— 缓存常用消息
 * @author huangtaiji
 * @date 2025/10/26
 */
public final class FixedMessagePool implements MessagePool {
	/**
	 * 缓存数据引擎
	 */
	private final Map<ByteBuf, SimpleStringMessage> byteBufToSimpleStrings;
	private final Map<ByteBuf, ErrorMessage> byteBufToErrors;
	private final Map<ByteBuf, IntegerMessage> byteBufToIntegers;
	private final Map<Long, byte[]> longToBytes;

	/**
	 * 缓存简单字符串
	 */
	public enum ReplyKey{
		OK, PONG, QUEUED
	}

	/**
	 * 缓存错误信息
	 */
	public enum ErrorKey {
		ERR("ERR"),
		ERR_IDX("ERR index out of range"),
		ERR_NOKEY("ERR no such key"),
		ERR_SAMEOBJ("ERR source and destination objects are the same"),
		ERR_SYNTAX("ERR syntax error"),
		BUSY("BUSY Redis is busy running a script. You can only call SCRIPT KILL or SHUTDOWN NOSAVE."),
		BUSYKEY("BUSYKEY Target key name already exists."),
		EXECABORT("EXECABORT Transaction discarded because of previous errors."),
		LOADING("LOADING Redis is loading the dataset in memory"),
		MASTERDOWN("MASTERDOWN Link with MASTER is down and slave-serve-stale-data is set to 'no'."),
		MISCONF("MISCONF Redis is configured to save RDB snapshots, but is currently not able to persist on disk. " +
				"Commands that may modify the data set are disabled. Please check Redis logs for details " +
				"about the error."),
		NOREPLICAS("NOREPLICAS Not enough good slaves to write."),
		NOSCRIPT("NOSCRIPT No matching script. Please use EVAL."),
		OOM("OOM command not allowed when used memory > 'maxmemory'."),
		READONLY("READONLY You can't write against a read only slave."),
		WRONGTYPE("WRONGTYPE Operation against a key holding the wrong kind of value"),
		NOT_AUTH("NOAUTH Authentication required.");


		private final String msg;

		ErrorKey(String msg) {
			this.msg = msg;
		}

		@Override
		public String toString() {
			return msg;
		}
	}

	/**
	 * 缓存整数的区间
	 */
	private static final long MIN_CACHED_INTEGER_NUMBER = CacheForgeConstants.NULL_VALUE;
	private static final long MAX_CACHED_INTEGER_NUMBER = 128;
	private static final int SIZE_CACHED_INTEGER_NUMBER = (int) (MAX_CACHED_INTEGER_NUMBER - MIN_CACHED_INTEGER_NUMBER);

	public static final FixedMessagePool INSTANCE = new FixedMessagePool();

	private FixedMessagePool() {
		// 缓存简单字符串
		byteBufToSimpleStrings = new HashMap<ByteBuf, SimpleStringMessage>(ReplyKey.values().length, 1.0f);
		for (ReplyKey value : ReplyKey.values()) {
			ByteBuf key = Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(
					value.name().getBytes(CharsetUtil.UTF_8))).asReadOnly();
			SimpleStringMessage msg = new SimpleStringMessage(new String(Unpooled.unreleasableBuffer(
					Unpooled.wrappedBuffer(value.name().getBytes(CharsetUtil.UTF_8))).array()));

			byteBufToSimpleStrings.put(key, msg);
		}

		// 缓存错误信息
		byteBufToErrors = new HashMap<ByteBuf, ErrorMessage>(ErrorKey.values().length, 1.0f);
		for (ErrorKey value : ErrorKey.values()) {
			ByteBuf key = Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(
					value.toString().getBytes(CharsetUtil.UTF_8))).asReadOnly();
			ErrorMessage msg = new ErrorMessage(new String(Unpooled.unreleasableBuffer(
					Unpooled.wrappedBuffer(value.toString().getBytes(CharsetUtil.UTF_8))).array()));

			byteBufToErrors.put(key, msg);
		}

		// 缓存整数
		byteBufToIntegers = new HashMap<ByteBuf, IntegerMessage>(SIZE_CACHED_INTEGER_NUMBER, 1.0f);
		longToBytes = new HashMap<Long, byte[]>(SIZE_CACHED_INTEGER_NUMBER, 1.0f);
		for (long value = MIN_CACHED_INTEGER_NUMBER; value < MAX_CACHED_INTEGER_NUMBER; value++) {
			byte[] keyBytes = CacheForgeCodecUtil.longToAsciiBytes(value);
			ByteBuf keyBuf = Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(keyBytes)).asReadOnly();
			IntegerMessage msg = new IntegerMessage(value);

			byteBufToIntegers.put(keyBuf, msg);
			longToBytes.put(value, keyBytes);
		}

	}







	@Override
	public SimpleStringMessage getSimpleString(ByteBuf content) {
		return byteBufToSimpleStrings.get(content);
	}

	@Override
	public ErrorMessage getError(ByteBuf content) {
		return byteBufToErrors.get(content);
	}

	@Override
	public IntegerMessage getInteger(ByteBuf value) {
		return byteBufToIntegers.get(value);
	}

	@Override
	public byte[] getBytesOfInteger(long value) {
		return longToBytes.get(value);
	}
}
