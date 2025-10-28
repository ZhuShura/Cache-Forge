package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.repo.StringStore;
import fun.redis.cacheforge.utils.MessageUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import static fun.redis.cacheforge.utils.MessageUtil.*;


/**
 * strlen命令处理器
 * @author huangtaiji
 * @date 2025/10/28
 */
@Slf4j
public class StrLenCommandHandler implements ReadCommandHandler {

	@Override
	public void handle(ChannelHandlerContext ctx, Command command) {
		try {
			String[] args = command.getArgs();
			if (args.length == 1) {
				String key = args[0];
				String value = StringStore.get(key);
				if (value == null || value.isEmpty()) {
					ctx.writeAndFlush(toIntegerMessage(0));
					return;
				}
				ctx.writeAndFlush(toIntegerMessage(value.length()));
			} else {
				log.error("strlen命令参数错误");
				// todo
				ctx.writeAndFlush(toErrorMessage(MessageUtil.Err.ERR));
			}
		} catch (Exception e) {
			log.error("strlen命令异常{}", String.valueOf(e));
			// todo
			ctx.writeAndFlush(toErrorMessage(MessageUtil.Err.ERR));
		}
	}
}
