package fun.redis.cacheforge.protocol.model.bulkString;

/**
 * 最后一个bulk string内容
 * @author huangtaiji
 * @date 2025/10/26
 */
public interface LastBulkStringContent extends BulkStringContent {

	/**
	 * 最后一个bulk string内容
	 */
	LastBulkStringContent EMPTY_LAST_CONTENT = new LastBulkStringContent() {};
}
