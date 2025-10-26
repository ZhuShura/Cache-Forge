package fun.redis.cacheforge.protocol;

import fun.redis.cacheforge.common.CacheForgeCodecException;
import fun.redis.cacheforge.common.CacheForgeConstants;
import fun.redis.cacheforge.common.MessageType;
import fun.redis.cacheforge.protocol.model.header.ArrayHeaderMessage;
import fun.redis.cacheforge.protocol.model.header.BulkStringHeaderMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.IntegerMessage;
import fun.redis.cacheforge.protocol.model.Message;
import fun.redis.cacheforge.protocol.model.bulkString.DefaultBulkStringContent;
import fun.redis.cacheforge.protocol.model.bulkString.DefaultLastBulkStringContent;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.ErrorMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.InlineCommandMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.SimpleStringMessage;
import fun.redis.cacheforge.protocol.model.pool.FixedMessagePool;
import fun.redis.cacheforge.protocol.model.pool.MessagePool;
import fun.redis.cacheforge.utils.CacheForgeCodecUtil;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ByteProcessor;


import java.nio.charset.StandardCharsets;
import java.util.List;



/**
 * RESP协议解码器
 * @author huangtaiji
 * @date 2025/10/26
 */
public class RespDecoder extends ByteToMessageDecoder {

	/**
	 * 解码器初始化参数
	 */
	private boolean decodeInlineCommands;    // 是否解码内联命令
	private int maxInlineMessageLength;      // 内联命令最大长度
	private MessagePool messagePool;         // 消息池缓存常用消息


	/**
	 * 常变的值
	 */
	private State state = State.DECODE_TYPE;
	private MessageType type;
	private int remainingBulkLength;

	private enum State {
		DECODE_TYPE,                         // 类型
		DECODE_INLINE,                       // 内联命令（包括SIMPLE_STRING、ERROR、INTEGER）
		DECODE_LENGTH,                       // 长度
		DECODE_BULK_STRING_EOL,              // CRLF
		DECODE_BULK_STRING_CONTENT           // BULK_STRING内容
	}

	public RespDecoder() { this(false); }

	public RespDecoder(boolean decodeInlineCommands) {
		this(CacheForgeConstants.INLINE_MESSAGE_MAX_LENGTH, FixedMessagePool.INSTANCE, decodeInlineCommands);
	}

	public RespDecoder(int maxInlineMessageLength, MessagePool messagePool) {
		this(maxInlineMessageLength, messagePool, false);
	}

	public RespDecoder(int maxInlineMessageLength, MessagePool messagePool, boolean decodeInlineCommands) {
		if (maxInlineMessageLength <= 0 || maxInlineMessageLength > CacheForgeConstants.INLINE_MESSAGE_MAX_LENGTH) {
			throw new CacheForgeCodecException("-ERROR inline message length is too long, expected: " + CacheForgeConstants.INLINE_MESSAGE_MAX_LENGTH);
		}
		this.maxInlineMessageLength = maxInlineMessageLength;
		this.messagePool = messagePool;
		this.decodeInlineCommands = decodeInlineCommands;
	}





	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		try {
			while (in.isReadable()) {
				switch (state) {
					case DECODE_TYPE:
						decodeType(in);
						break;
					case DECODE_INLINE:
						decodeInline(in, out);
						break;
					case DECODE_LENGTH:
						decodeLength(in, out);
						break;
					case DECODE_BULK_STRING_EOL:
						decodeBulkStringEOL(in, out);
						break;
					case DECODE_BULK_STRING_CONTENT:
						decodeBulkStringContent(in, out);
						break;
					default:
						throw new CacheForgeCodecException("-ERROR unknown state: " +  state);
				}
			}
		} catch (CacheForgeCodecException e) {
			resetDecoder();
			throw e;
		} catch (Exception e) {
			resetDecoder();
			throw new CacheForgeCodecException(e);
		}
	}

	/**
	 * 重置解码器
	 */
	private void resetDecoder() {
		state = State.DECODE_TYPE;
		remainingBulkLength = 0;
	}


	// ----------------具体解码方法--------------------

	/**
	 * 解码类型
	 */
	private void decodeType(ByteBuf in) throws Exception{
		type = MessageType.readForm(in, decodeInlineCommands);
		state = type.isInline() ? State.DECODE_INLINE : State.DECODE_LENGTH;
	}

	/**
	 * 解码内联命令
	 */
	private void decodeInline(ByteBuf in, List<Object> out) {
		ByteBuf line = readLine(in);
		if (line == null) {
			if (in.readableBytes() > maxInlineMessageLength) {
				throw new CacheForgeCodecException("-ERROR too long inline message, expected max length is " + maxInlineMessageLength);
			}
			return;
		}
		out.add(newInlineMessage(type, line));
		resetDecoder();
	}

	/**
	 * 根据类型新建内联命令消息
	 */
	private Message newInlineMessage(MessageType type, ByteBuf content) {
		switch (type) {
			case INLINE_COMMAND:
				return new InlineCommandMessage(content.toString(StandardCharsets.UTF_8));
			case SIMPLE_STRING: {
				SimpleStringMessage cached = messagePool.getSimpleString(content);
				return cached != null ? cached : new SimpleStringMessage(content.toString(StandardCharsets.UTF_8));
			}
			case ERROR: {
				ErrorMessage cached = messagePool.getError(content);
				return cached != null ? cached : new SimpleStringMessage(content.toString(StandardCharsets.UTF_8));
			}
			case INTEGER: {
				IntegerMessage cached = messagePool.getInteger(content);
				return cached != null ? cached : new IntegerMessage(CacheForgeCodecUtil.parseNumber(content));
			}
			default:
				throw new CacheForgeCodecException("-ERROR bad type: " + type);
		}
	}

	/**
	 * 解码长度
	 */
	private void decodeLength(ByteBuf in, List<Object> out) {
		ByteBuf line = readLine(in);
		if (line == null) {
			return;
		}
		final long length = CacheForgeCodecUtil.parseNumber(line);
		if (length < CacheForgeConstants.NULL_VALUE) {
			throw new CacheForgeCodecException("-ERROR length: " + length + ", expected length >= " + CacheForgeConstants.NULL_VALUE);
		}
		switch (type) {
			case ARRAY_HEADER:
				out.add(new ArrayHeaderMessage(length));
				resetDecoder();
				break;
			case BULK_STRING:
				if (length > CacheForgeConstants.MESSAGE_MAX_LENGTH) {
					throw new CacheForgeCodecException("-ERROR length: " + length + ", expected length <= " + CacheForgeConstants.MESSAGE_MAX_LENGTH);
				}
				remainingBulkLength = (int) length;
				decodeBulkString(in, out);
				break;
			default:
				throw new CacheForgeCodecException("-ERROR bad type: " + type);
		}
	}

	/**
	 * 处理特殊长度
	 * @code @default} 普通
	 */
	private void decodeBulkString(ByteBuf in, List<Object> out) {
		switch (remainingBulkLength) {
			case CacheForgeConstants.NULL_VALUE:
				out.add(FullBulkStringMessage.NULL_INSTANCE);
				resetDecoder();
				break;
			case 0:
				state = State.DECODE_BULK_STRING_EOL;
				decodeBulkStringEOL(in, out);
				break;
			default:
				out.add(new BulkStringHeaderMessage(remainingBulkLength));
				state = State.DECODE_BULK_STRING_CONTENT;
				decodeBulkStringContent(in, out);
				break;
		}
	}

	/**
	 * 解码CRLF
	 */
	private void decodeBulkStringEOL(ByteBuf in, List<Object> out) {
		if (in.readableBytes() < CacheForgeConstants.EOL_LENGTH) {
			return;
		}
		readEndOfLine(in);
		out.add(FullBulkStringMessage.EMPTY_INSTANCE);
		resetDecoder();
	}

	/**
	 * 解码Bulk String内容
	 */
	private void decodeBulkStringContent(ByteBuf in, List<Object> out) {
		final int readableBytes = in.readableBytes();
		if (remainingBulkLength == 0 && readableBytes < CacheForgeConstants.EOL_LENGTH) {
			return;
		}
		// 能一次性读完
		if (readableBytes >= remainingBulkLength + CacheForgeConstants.EOL_LENGTH) {
			ByteBuf content = in.readSlice(remainingBulkLength);
			readEndOfLine(in);
			out.add(new DefaultLastBulkStringContent(content.retain()));
			resetDecoder();
			return;
		}
		// 不能一次性读完
		int toRead = Math.min(remainingBulkLength, readableBytes);
		remainingBulkLength -= toRead;
		out.add(new DefaultBulkStringContent(in.readSlice(toRead).retain()));
	}


	// ------------------读取--------------------------

	/**
	 * 读取一行
	 */
	private static ByteBuf readLine(ByteBuf in) {
		if (!in.isReadable(CacheForgeConstants.EOL_LENGTH)) {
			return null;
		}
		final int lfIndex = in.forEachByte(ByteProcessor.FIND_LF);   // 遍历获取\n索引位置
		if (lfIndex < 0) {
			return null;
		}
		ByteBuf data = in.readSlice(lfIndex - 1 - in.readerIndex());    // 零拷贝读取数据
		readEndOfLine(in);
		return data;
	}

	/**
	 * 读取CRLF
	 */
	private static void readEndOfLine(ByteBuf in) {
		final short delimiter = in.readShort();
		if (CacheForgeConstants.EOL_LENGTH == delimiter) {
			return;
		}
		final byte[] bytes = CacheForgeCodecUtil.shortToBytes(delimiter);
		throw new CacheForgeCodecException("-ERROR delimiter[" + bytes[0] + ", " + bytes[1] + "], but expected CRLF");
	}

}
