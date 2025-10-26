package fun.redis.cacheforge.protocol.model.generalizedInline;

import fun.redis.cacheforge.protocol.model.Message;
import io.netty.util.internal.StringUtil;

public class IntegerMessage implements Message {
	private final long value;

	public IntegerMessage(long value) {
		this.value = value;
	}

	public long value() {
		return value;
	}

	@Override
	public String toString() {
		return new StringBuilder(StringUtil.simpleClassName(this))
				.append('[')
				.append("value=")
				.append(value)
				.append(']').toString();
	}
}
