package fun.redis.cacheforge.protocol.model.generalizedInline;

/**
 * 错误消息
 * @author huangtaiji
 * @date 2025/10/26
 */
public class ErrorMessage extends AbstractStringMessage {
	public ErrorMessage(String content) {
		super(content);
	}
}
