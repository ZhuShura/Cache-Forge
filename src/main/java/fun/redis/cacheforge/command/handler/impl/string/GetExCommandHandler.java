package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.CommandRegistry;
import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.common.CacheForgeConstants.ExpireUnit;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.storage.repo.GlobalStore;
import fun.redis.cacheforge.storage.repo.StringStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * getex命令处理器
 * @author huangtaji
 * @date 2025/10/28
 */
@Slf4j
public class GetExCommandHandler implements WriteCommandHandler {

	@Override
	public void handle(ChannelHandlerContext ctx, Command command) {
		try {
			String[] args = command.getArgs();
			if (args.length == 1) {
				CommandRegistry.get("get").handle(ctx, new Command("get", args));
			} else if (args.length == 3) {
				String key = args[0];
				ExpireUnit expireUnit = ExpireUnit.valueOf(args[1].toUpperCase());
				long expireTime = Long.parseLong(args[2]);

				String value = StringStore.get(key);
				if (value == null) {
					ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
					return;
				}
				GlobalStore.setExpire(key, expireTime*expireUnit.value());
				ctx.writeAndFlush(toFullBulkStringMessage(value));
			} else {
				log.error("getex命令参数错误");
				// todo
				ctx.writeAndFlush(toErrorMessage(Err.ERR));
			}
		} catch (Exception e) {
			log.error("getex命令异常{}", String.valueOf(e));
			// todo
			ctx.writeAndFlush(toErrorMessage(Err.ERR));
		}
	}
}
