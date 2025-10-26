package fun.redis.cacheforge.protocol.model.pool;

import fun.redis.cacheforge.protocol.model.generalizedInline.ErrorMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.IntegerMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.SimpleStringMessage;
import io.netty.buffer.ByteBuf;

/**
 * 消息池接口
 * @author huangtaiji
 * @date 2025/10/26
 */
public interface MessagePool {

	SimpleStringMessage getSimpleString(ByteBuf content);

	ErrorMessage getError(ByteBuf content);

	IntegerMessage getInteger(ByteBuf content);
}
