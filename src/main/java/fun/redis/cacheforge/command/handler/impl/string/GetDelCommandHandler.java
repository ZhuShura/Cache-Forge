package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.storage.repo.StringStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * getdel命令处理器
 * @author huangtaji
 * @date 2025/10/28
 */
@Slf4j
public class GetDelCommandHandler implements WriteCommandHandler {
	@Override
	public void handle(ChannelHandlerContext ctx, Command command) {
		try {
			String[] args = command.getArgs();
			if (args.length == 1) {
				String key = args[0];
				String value = StringStore.get(key);
				if (value == null) {
					ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
					return;
				}
				StringStore.del(key);
				ctx.writeAndFlush(toFullBulkStringMessage(value));
			} else {
				log.error("getdel命令参数错误");
				// todo
				ctx.writeAndFlush(toErrorMessage(Err.ERR));
			}
		} catch (Exception e) {
			log.error("getdel命令异常{}", String.valueOf(e));
			// todo
			ctx.writeAndFlush(toErrorMessage(Err.ERR));
		}
	}
}
