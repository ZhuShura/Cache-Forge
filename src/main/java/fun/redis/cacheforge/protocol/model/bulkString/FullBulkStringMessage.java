package fun.redis.cacheforge.protocol.model.bulkString;

/**
 * 完整字符串消息
 * @author huangtaiji
 * @date 2025/10/26
 */
public class FullBulkStringMessage implements LastBulkStringContent {


	public static final FullBulkStringMessage EMPTY_INSTANCE = new FullBulkStringMessage();

	public static final FullBulkStringMessage NULL_INSTANCE = new FullBulkStringMessage();

}
