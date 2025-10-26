package fun.redis.cacheforge.protocol.model.generalizedInline;

/**
 * 内联命令消息
 * @author huangtaiji
 * @date 2025/10/26
 */
public class InlineCommandMessage extends AbstractStringMessage {
	public InlineCommandMessage(String content) {
		super(content);
	}
}
