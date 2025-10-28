package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.repo.StringStore;
import fun.redis.cacheforge.utils.MessageUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * decr 命令处理器
 * @author hua
 * @date 2025/10/27
 */
@Slf4j
public class DecrCommandHandler implements WriteCommandHandler {
	@Override
	public void handle(ChannelHandlerContext ctx, Command command) {
		try {
			String[] args = command.getArgs();
			if (args.length == 1) {
				String key = args[0];
				String value = StringStore.get(key);
				int result;
				if (value == null) {
					result = -1;
				} else {
					result = Integer.parseInt(value) - 1;
				}
				StringStore.set(key, String.valueOf(result));
				ctx.writeAndFlush(toIntegerMessage(result));
			} else {
				log.error("decr命令参数错误");
				// todo
				ctx.writeAndFlush(toErrorMessage(Err.ERR));
			}
		} catch (Exception e) {
			log.error("decr命令异常{}", String.valueOf(e));
			// todo
			ctx.writeAndFlush(toErrorMessage(Err.ERR));
		}
	}
}
