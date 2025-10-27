package fun.redis.cacheforge.protocol;

import fun.redis.cacheforge.common.CacheForgeCodecException;
import fun.redis.cacheforge.common.CacheForgeConstants;
import fun.redis.cacheforge.common.MessageType;
import fun.redis.cacheforge.protocol.model.Message;
import fun.redis.cacheforge.protocol.model.array.ArrayHeaderMessage;
import fun.redis.cacheforge.protocol.model.array.ArrayMessage;
import fun.redis.cacheforge.protocol.model.bulkString.BulkStringContent;
import fun.redis.cacheforge.protocol.model.bulkString.BulkStringHeaderMessage;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.protocol.model.bulkString.LastBulkStringContent;
import fun.redis.cacheforge.protocol.model.generalizedInline.ErrorMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.InlineCommandMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.IntegerMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.SimpleStringMessage;
import fun.redis.cacheforge.protocol.model.pool.FixedMessagePool;
import fun.redis.cacheforge.protocol.model.pool.MessagePool;
import fun.redis.cacheforge.utils.CacheForgeCodecUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.internal.ObjectUtil;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * RESP编码器
 * @author huangtaiji
 * @date 2025/10/27
 */
public class RespEncoder extends MessageToMessageEncoder<Message> {

	private final MessagePool messagePool;

	public RespEncoder() {
		this.messagePool = FixedMessagePool.INSTANCE;
	}

	public RespEncoder(MessagePool messagePool) {
		this.messagePool = ObjectUtil.checkNotNull(messagePool, "messagePool");
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
		try {
			writeMessage(ctx.alloc(), msg, out);
		} catch (CacheForgeCodecException e) {
			throw e;
		} catch (Exception e) {
			throw new CacheForgeCodecException(e);
		}
	}


	// -------------将消息写出-------A代表直接进入writeMessage方法的，B代表没进入------------
	private void writeMessage(ByteBufAllocator alloc, Message msg, List<Object> out) {
		if (msg instanceof InlineCommandMessage) {
			writeInlineCommandMessage(alloc, (InlineCommandMessage)msg, out);
		} else if (msg instanceof SimpleStringMessage) {
			writeSimpleStringMessage(alloc, (SimpleStringMessage)msg, out);
		} else if (msg instanceof ErrorMessage) {
			writeErrorMessage(alloc, (ErrorMessage)msg, out);
		} else if (msg instanceof IntegerMessage) {
			writeIntegerMessage(alloc, (IntegerMessage)msg, out);
		} else if (msg instanceof FullBulkStringMessage) {
			writeFullBulkStringMessage(alloc, (FullBulkStringMessage)msg, out);
		} else if (msg instanceof ArrayMessage) {
			writeArrayMessage(alloc, (ArrayMessage)msg, out);
		} else if (msg instanceof ArrayHeaderMessage) {
			writeArrayHeader(alloc, (ArrayHeaderMessage)msg, out);
		} else if (msg instanceof BulkStringHeaderMessage) {
			writeBulkStringHeader(alloc, (BulkStringHeaderMessage)msg, out);
		} else if (msg instanceof BulkStringContent) {
			writeBulkStringContent(alloc, (BulkStringContent)msg, out);
		} else {
			throw new CacheForgeCodecException("-ERROR unknown message type: " + msg.getClass().getName());
		}
	}

	/**
	 * A写入命令行消息
	 */
	private void writeInlineCommandMessage(ByteBufAllocator alloc, InlineCommandMessage msg, List<Object> out) {
		writeString(alloc, MessageType.INLINE_COMMAND, msg.content(), out);
	}

	/**
	 * A写入字符串消息
	 */
	private void writeSimpleStringMessage(ByteBufAllocator alloc, SimpleStringMessage msg, List<Object> out) {
		writeString(alloc, MessageType.SIMPLE_STRING, msg.content(), out);
	}

	/**
	 * A写入整数消息
	 */
	private void writeErrorMessage(ByteBufAllocator alloc, ErrorMessage msg, List<Object> out) {
		writeString(alloc, MessageType.ERROR, msg.content(), out);
	}

	/**
	 * A写入字符串消息
	 */
	private void writeString(ByteBufAllocator alloc, MessageType type, String content, List<Object> out) {
		ByteBuf buf = alloc.ioBuffer(type.length() + ByteBufUtil.utf8MaxBytes(content) + CacheForgeConstants.EOL_LENGTH);
		type.writeTo(buf);                                  // 写入类型
		ByteBufUtil.writeUtf8(buf, content);                // 写入内容
		buf.writeShort(CacheForgeConstants.EOL_SHORT);      // 写入CRLF
		out.add(buf);
	}

	/**
	 * A写入整数消息
	 */
	private void writeIntegerMessage(ByteBufAllocator alloc, IntegerMessage msg, List<Object> out) {
		MessageType type = MessageType.INTEGER;
		ByteBuf buf = alloc.ioBuffer(type.length()+ CacheForgeConstants.LONG_MAX_LENGTH + CacheForgeConstants.EOL_LENGTH);
		type.writeTo(buf);                                  // 写入类型
		buf.writeBytes(numberToBytes(msg.value()));         // 写入内容
		buf.writeShort(CacheForgeConstants.EOL_SHORT);      // 写入CRLF
		out.add(buf);
	}

	/**
	 * A写入 bulk string 消息
	 */
	private void writeFullBulkStringMessage(ByteBufAllocator alloc, FullBulkStringMessage msg, List<Object> out) {
		MessageType type = MessageType.BULK_STRING;
		if (msg.isNull()) {
			ByteBuf buf = alloc.ioBuffer(type.length() + CacheForgeConstants.NULL_LENGTH + CacheForgeConstants.EOL_LENGTH);
			type.writeTo(buf);                              // 写入类型
			buf.writeShort(CacheForgeConstants.NULL_SHORT); // 写入nil值
			buf.writeShort(CacheForgeConstants.EOL_SHORT);  // 写入CRLF
			out.add(buf);
		} else  {
			ByteBuf headerBuf = alloc.ioBuffer(type.length() + CacheForgeConstants.LONG_MAX_LENGTH + CacheForgeConstants.EOL_LENGTH);
			type.writeTo(headerBuf);                                                // 写入header类型
			headerBuf.writeBytes(numberToBytes(msg.content().readableBytes()));     // 写入header长度
			headerBuf.writeShort(CacheForgeConstants.EOL_SHORT);                    // 写入headerCRLF

			out.add(headerBuf);                                                                                 // 写入header
			out.add(msg.content().retain());                                                                    // 写入content（续命）
			out.add(alloc.ioBuffer(CacheForgeConstants.EOL_LENGTH).writeShort(CacheForgeConstants.EOL_SHORT));  // 写入CRLF
		}
	}

	/**
	 * A写入数组消息————递归处理内部消息
	 */
	private void writeArrayMessage(ByteBufAllocator alloc, ArrayMessage msg, List<Object> out) {
		if (msg.isNull()) {
			writeArrayHeader(alloc, msg.isNull(), CacheForgeConstants.NULL_VALUE, out);
		} else {
			writeArrayHeader(alloc, msg.isNull(), msg.children().size(), out);
			for (Message child : msg.children()) {
				writeMessage(alloc, child, out);
			}
		}
	}

	/**
	 * B写入数组消息头
	 */
	private void writeArrayHeader(ByteBufAllocator alloc, boolean isNull, long length, List<Object> out) {
		MessageType type = MessageType.ARRAY_HEADER;
		if (isNull) {
			ByteBuf buf = alloc.ioBuffer(type.length() + CacheForgeConstants.NULL_LENGTH + CacheForgeConstants.EOL_LENGTH);
			type.writeTo(buf);
			buf.writeShort(CacheForgeConstants.NULL_SHORT);
			buf.writeShort(CacheForgeConstants.EOL_SHORT);
			out.add(buf);
		} else {
			ByteBuf buf = alloc.ioBuffer(type.length() + CacheForgeConstants.LONG_MAX_LENGTH + CacheForgeConstants.EOL_LENGTH);
			type.writeTo(buf);
			buf.writeBytes(numberToBytes(length));
			buf.writeShort(CacheForgeConstants.EOL_SHORT);
			out.add(buf);
		}
	}

	/**
	 * A写入数组消息头
	 */
	private void writeArrayHeader(ByteBufAllocator alloc,  ArrayHeaderMessage msg, List<Object> out) {
		writeArrayHeader(alloc, msg.isNull(), msg.length(), out);
	}

	/**
	 * A写入bulk string消息头
	 */
	private void writeBulkStringHeader(ByteBufAllocator alloc, BulkStringHeaderMessage msg, List<Object> out) {
		MessageType type = MessageType.BULK_STRING;
		if (msg.isNull()) {
			ByteBuf buf = alloc.ioBuffer(type.length() + CacheForgeConstants.NULL_LENGTH + CacheForgeConstants.EOL_LENGTH);
			buf.writeShort(CacheForgeConstants.NULL_SHORT);                                 // 写入nil值
			out.add(buf);
		} else {
			ByteBuf buf = alloc.ioBuffer(type.length() + CacheForgeConstants.LONG_MAX_LENGTH + CacheForgeConstants.EOL_LENGTH);
			buf.writeBytes(numberToBytes(msg.length()));                                    // 写入长度
			buf.writeShort(CacheForgeConstants.EOL_SHORT);                                  // 写入CRLF
			out.add(buf);
		}
	}

	/**
	 * A写入bulk string消息内容
	 */
	private void writeBulkStringContent (ByteBufAllocator alloc, BulkStringContent msg, List<Object> out) {
		out.add(msg.content().retain());
		if (msg instanceof LastBulkStringContent) {                                         // bulk_string末尾写入CRLF
			out.add(alloc.ioBuffer(CacheForgeConstants.EOL_LENGTH).writeShort(CacheForgeConstants.EOL_SHORT));
		}
	}


	// ------------转换工具-------------------

	private byte[] numberToBytes(long number) {
		byte[] bytes = messagePool.getBytesOfInteger(number);
		return bytes != null ? bytes : CacheForgeCodecUtil.longToAsciiBytes(number);
	}

}
