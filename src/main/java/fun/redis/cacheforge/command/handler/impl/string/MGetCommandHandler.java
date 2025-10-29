package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.Message;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.storage.repo.StringStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * mget命令处理器
 * @author huangtaiji
 * @date 2025/10/29
 */
@Slf4j
public class MGetCommandHandler implements ReadCommandHandler {
	@Override
	public void handle(ChannelHandlerContext ctx, Command command) {
		try {
			String[] args = command.getArgs();
			if (args.length >= 1) {
				List<FullBulkStringMessage> children = new ArrayList<>();
				for (String key : args) {
					String value = StringStore.get(key);
					if (value == null) {
						children.add(FullBulkStringMessage.NULL_INSTANCE);
					} else {
						children.add(toFullBulkStringMessage(value));
					}
				}
				ctx.writeAndFlush(toArrayMessage(children.toArray(new FullBulkStringMessage[0])));
			} else {
				log.error("mget命令参数错误");
				// todo
				ctx.writeAndFlush(toErrorMessage(Err.ERR));
			}
		} catch (Exception e) {
			log.error("mget命令异常{}", String.valueOf(e));
			// todo
			ctx.writeAndFlush(toErrorMessage(Err.ERR));
		}
	}
}
