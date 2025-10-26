package fun.redis.cacheforge.common;


import io.netty.buffer.ByteBuf;
import io.netty.util.internal.UnstableApi;

/**
 * 消息类型枚举
 * @author huangtaiji
 * @date 2025/10/26
 */
@UnstableApi
public enum MessageType {

	INLINE_COMMAND(null, true),
	SIMPLE_STRING((byte)'+', true),
	ERROR((byte)'-', true),
	INTEGER((byte)':', true),
	BULK_STRING((byte)'$', false),
	ARRAY_HEADER((byte)'*', false);

	private final Byte type;
	private final boolean isInline;

	MessageType(Byte type, boolean isInline) {
		this.type = type;
		this.isInline = isInline;
	}

	/**
	 * @return 类型字节长度
	 */
	public int length() {
		return type != null ? CacheForgeConstants.TYPE_LENGTH : 0;
	}

	/**
	 * @return 是否是内联命令
	 */
	public boolean isInline() {
		return isInline;
	}


	/**
	 * 读取消息类型
	 * {@code @if} 内联则回退一字节，否则不回退
	 * @param in 输入流
	 * @param decodeInlineCommands 是否解码内联命令
	 * @return 消息类型
	 */
	public static MessageType readForm(ByteBuf in, boolean decodeInlineCommands) {
		final int initialIndex = in.readerIndex();
		final MessageType type = valueOf(in.readByte());
		if (type == INLINE_COMMAND) {
			if (!decodeInlineCommands) {
				throw new CacheForgeCodecException("-ERROR cannot decode inline commands");
			}
			in.readerIndex(initialIndex);
		}
		return type;
	}



	/**
	 * 根据类型字节获取消息类型枚举
	 * @param type 类型字节
	 * @return 消息类型枚举
	 */
	private static MessageType valueOf(byte type) {
		switch (type) {
			case '+':
				return SIMPLE_STRING;
			case '-':
				return ERROR;
			case ':':
				return INTEGER;
			case '$':
				return BULK_STRING;
			case '*':
				return ARRAY_HEADER;
			default:
				return null;
		}
	}

}
