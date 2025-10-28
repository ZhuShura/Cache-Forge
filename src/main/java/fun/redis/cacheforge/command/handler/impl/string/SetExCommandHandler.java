package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.CommandRegistry;
import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * setex命令处理器
 * @author huangtaiji
 * @date 2025/10/28
 */
@Deprecated
@Slf4j
public class SetExCommandHandler implements WriteCommandHandler {

	@Override
	public void handle(ChannelHandlerContext ctx, Command command) {
		try {
			String[] args = command.getArgs();
			if (args.length == 3) {
				String key = args[0];
				long expireTime = Long.parseLong(args[1]);
				String value = args[2];
				CommandRegistry.get("set").handle(ctx, new Command("set", new String[]{key, value, "EX", String.valueOf(expireTime)}));
			} else {
				log.error("setex命令参数错误");
				// todo
				ctx.writeAndFlush(toErrorMessage(Err.ERR));
			}
		} catch (Exception e) {
			log.error("setex命令异常");
			// todo
			ctx.writeAndFlush(toErrorMessage(Err.ERR));
		}
	}
}
