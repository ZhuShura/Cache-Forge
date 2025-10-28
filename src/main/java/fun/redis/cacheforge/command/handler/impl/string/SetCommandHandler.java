package fun.redis.cacheforge.command.handler.impl.string;


import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.common.CacheForgeConstants.ExpireUnit;
import fun.redis.cacheforge.storage.repo.StringStore;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import static fun.redis.cacheforge.utils.MessageUtil.*;


/**
 * set命令处理器
 * @author huangtaiji
 * @date 2025/10/27
 */
@Slf4j
public class SetCommandHandler implements WriteCommandHandler {
	@Override
	public void handle(ChannelHandlerContext ctx, Command command) {
		try {
			String[] args = command.getArgs();
			if (args.length == 2) {
				String key = command.getArgs()[0];
				String value = command.getArgs()[1];
				StringStore.set(key, value);
				ctx.writeAndFlush(toSimpleStringMessage(Reply.OK));
			} else if (args.length == 4) {
				String key = command.getArgs()[0];
				String value = command.getArgs()[1];
				ExpireUnit expireUnit = ExpireUnit.valueOf(command.getArgs()[2]);
				long expireTime = Long.parseLong(command.getArgs()[3]);
				StringStore.set(key, value, expireUnit, expireTime);
				ctx.writeAndFlush(toSimpleStringMessage(Reply.OK));
			} else {
				log.error("set命令参数错误");
				//todo
				ctx.writeAndFlush(toErrorMessage(Err.ERR));
			}
		} catch (Exception e) {
            log.error("set命令异常{}", String.valueOf(e));
			// todo
			ctx.writeAndFlush(toErrorMessage(Err.ERR));
		}
	}
}
