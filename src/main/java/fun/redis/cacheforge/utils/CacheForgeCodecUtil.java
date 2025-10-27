package fun.redis.cacheforge.utils;

import fun.redis.cacheforge.common.CacheForgeCodecException;
import fun.redis.cacheforge.common.CacheForgeConstants;
import io.netty.buffer.ByteBuf;
import io.netty.util.ByteProcessor;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.PlatformDependent;

import java.nio.charset.StandardCharsets;

/**
 * 编解码工具类
 * @author huangtaiji
 * @date 2025/10/26
 */
public class CacheForgeCodecUtil {

	private static final ToPositiveLongProcessor processor = new ToPositiveLongProcessor();

	private CacheForgeCodecUtil(){}



	/**
	 * long转byte[](Ascii)
	 */
	public static byte[] longToAsciiBytes(long value) {
		return Long.toString(value).getBytes(StandardCharsets.US_ASCII);
	}


	/**
	 * 解析数字
	 */
	public static long parseNumber (ByteBuf numBuf) {
		final int readableBytes = numBuf.readableBytes();
		final boolean negative = readableBytes > 0 && numBuf.getByte(numBuf.readerIndex()) == '-';
		final int extraOneByteForNegative = negative ? 1 : 0;
		if (readableBytes <= extraOneByteForNegative) {
			throw new CacheForgeCodecException("-ERROR no number to parse: " + numBuf.toString(CharsetUtil.US_ASCII));
		}
		if (readableBytes > CacheForgeConstants.POSITIVE_LONG_MAX_LENGTH + extraOneByteForNegative) {
			throw new CacheForgeCodecException("-ERROR too many characters to be a valid RESP Integer: " +
					numBuf.toString(CharsetUtil.US_ASCII));
		}
		if (negative) {
			return -parsePositiveNumber(numBuf.skipBytes(extraOneByteForNegative));
		}
		return parsePositiveNumber(numBuf);
	}

	/**
	 * 解析正整数（Long）
	 */
	private static long parsePositiveNumber (ByteBuf numBuf) {
		processor.reset();
		numBuf.forEachByte(processor);
		return processor.content();
	}

	/**
	 * 解析正整数（Long）处理器
	 */
	private static final class ToPositiveLongProcessor implements ByteProcessor {
		private long result;

		@Override
		public boolean process(byte value) throws Exception {
			if (value < '0' || value > '9') {
				throw new CacheForgeCodecException("bad byte in number: " + value);
			}
			result = result * 10 + (value - '0');
			return true;
		}

		public long content() {
			return result;
		}

		public void reset() {
			result = 0;
		}
	}

	/**
	 * 字符生成short类型
	 */
	public static short makeShort(char first, char second) {
		return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ?
				(short) ((second << 8) | first) : (short) ((first << 8) | second);
	}

	/**
	 * short转byte[]
	 * 实现大端序和小端序不同字节序的处理方式
	 */
	public static byte[] shortToBytes(short value) {
		byte[] bytes = new byte[2];
		if (PlatformDependent.BIG_ENDIAN_NATIVE_ORDER) {
			bytes[1] = (byte) ((value >> 8) & 0xff);
			bytes[0] = (byte) (value & 0xff);
		} else {
			bytes[0] = (byte) ((value >> 8) & 0xff);
			bytes[1] = (byte) (value & 0xff);
		}
		return bytes;
	}
}
