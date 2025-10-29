package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.repo.StringStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * msetnx命令处理器
 * @author huangtaiji
 * @date 2025/10/29
 */
@Slf4j
public class MSetNxCommandHandler implements WriteCommandHandler {

	@Override
	public void handle(ChannelHandlerContext ctx, Command command) {
		try {
			String[] args = command.getArgs();
			if (args.length >= 2 && args.length % 2 == 0) {
				for (int i = 0; i < args.length; i += 2) {
					String key = args[i];
					String oldValue = StringStore.get(key);
					if (oldValue != null) {
						ctx.writeAndFlush(toIntegerMessage(0));
						return;
					}
				}

				for (int i = 0; i < args.length; i += 2) {
					String key = args[i];
					String value = args[i + 1];
					StringStore.set(key, value);
				}
				ctx.writeAndFlush(toIntegerMessage(1));
			} else {
				log.error("msetnx命令参数错误");
				// todo
				ctx.writeAndFlush(toErrorMessage(Err.ERR));
			}
		} catch (Exception e) {
			log.error("msetnx命令异常{}", String.valueOf(e));
			// todo
			ctx.writeAndFlush(toErrorMessage(Err.ERR));
		}
	}
}
