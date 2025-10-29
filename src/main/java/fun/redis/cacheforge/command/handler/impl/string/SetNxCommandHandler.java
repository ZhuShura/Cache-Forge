package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.CommandRegistry;
import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import static fun.redis.cacheforge.utils.MessageUtil.*;


/**
 * setnx命令处理器
 * @author huangtaiji
 * @date 2025/10/28
 */
@Deprecated
@Slf4j
public class SetNxCommandHandler implements WriteCommandHandler {


	@Override
	public void handle(ChannelHandlerContext ctx, Command command) {
		try {
			String[] args = command.getArgs();
			if (args.length == 2) {
				String key = args[0];
				String value = args[1];
				CommandRegistry.get("set").handle(ctx, new Command("set", new String[]{key, value, "NX"}));
			} else {
				log.error("setnx命令参数错误");
				// todo
				ctx.writeAndFlush(toErrorMessage(Err.ERR));
			}
		} catch (Exception e) {
			log.error("setnx命令异常{}", String.valueOf(e));
			// todo
			ctx.writeAndFlush(toErrorMessage(Err.ERR));
		}
	}
}
