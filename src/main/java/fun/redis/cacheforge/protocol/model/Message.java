package fun.redis.cacheforge.protocol.model;

import io.netty.buffer.ByteBuf;

/**
 * 解码出的消息
 * @author huangtaiji
 * @date 2025/10/26
 */
public class Message {
	private final String msg;

	public Message(ByteBuf msg) {
		this.msg = msg.toString();
	}

	public String getMsg() {
		return msg;
	}
}
