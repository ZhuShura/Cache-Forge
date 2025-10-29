package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.repo.StringStore;
import fun.redis.cacheforge.utils.MessageUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * append命令处理器
 * @author huangtaiji
 * @date 2025/10/28
 */
@Slf4j
public class AppendCommandHandler implements WriteCommandHandler {
	@Override
	public void handle(ChannelHandlerContext ctx, Command command) {
		try {
			String[] args = command.getArgs();
			if (args.length == 2) {
				String key = args[0];
				String value = args[1];

				String oldValue = StringStore.get(key);
				long length;
				if (oldValue == null) {
					StringStore.set(key, value);
					length = value.length();
				} else {
					StringStore.set(key, oldValue + value);
					length = oldValue.length() + value.length();
				}
				ctx.writeAndFlush(toIntegerMessage(length));
			} else {
				log.error("append命令参数错误");
				// todo
				ctx.writeAndFlush(toErrorMessage(Err.ERR));
			}
		} catch (Exception e) {
			log.error("append命令异常{}", String.valueOf(e));
			// todo
			ctx.writeAndFlush(toErrorMessage(Err.ERR));
		}
	}
}
