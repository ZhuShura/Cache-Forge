package fun.redis.cacheforge.protocol.model.generalizedInline;

import fun.redis.cacheforge.protocol.model.Message;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

/**
 * 字符串消息抽象类
 * 实现类：{@link SimpleStringMessage}、{@link ErrorMessage}、{@link InlineCommandMessage}
 * @author huangtaiji
 * @date 2025/10/26
 */
public abstract class AbstractStringMessage implements Message {
	private final String content;

	protected AbstractStringMessage(String content) {
		this.content = ObjectUtil.checkNotNull(content, "content");
	}

	public final String content() {
		return content;
	}

	@Override
	public String toString() {
		return new StringBuilder(StringUtil.simpleClassName(this))
				.append('[')
				.append("content=")
				.append(content)
				.append(']').toString();
	}
}
