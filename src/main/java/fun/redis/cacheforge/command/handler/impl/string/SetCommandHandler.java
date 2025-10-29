package fun.redis.cacheforge.command.handler.impl.string;


import fun.redis.cacheforge.command.CommandRegistry;
import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.common.CacheForgeConstants.ExpireUnit;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.storage.repo.GlobalStore;
import fun.redis.cacheforge.storage.repo.StringStore;

import fun.redis.cacheforge.utils.TimeUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import static fun.redis.cacheforge.utils.MessageUtil.*;


/**
 * set命令处理器
 * @author huangtaiji
 * @date 2025/10/28
 */
@Slf4j
public class SetCommandHandler implements WriteCommandHandler {
	@Override
	public void handle(ChannelHandlerContext ctx, Command command) {
		try {
			String[] args = command.getArgs();
			if (args.length == 2) {
				String key = args[0];
				String value = args[1];

				StringStore.set(key, value);

				ctx.writeAndFlush(toSimpleStringMessage(Reply.OK));
			} else if (args.length == 3) {
				String key = args[0];
				String value = args[1];
				Condition condition = Condition.valueOf(args[2].toUpperCase()); // 如果不是NX/XX会抛出异常IllegalArgumentException

				String oldValue = StringStore.get(key);
				if (condition == Condition.NX && oldValue != null || condition == Condition.XX && oldValue == null) {
					ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
					return;
				}
				StringStore.set(key, value);

				ctx.writeAndFlush(toSimpleStringMessage(Reply.OK));
			} else if (args.length == 4) {
				String key = args[0];
				String value = args[1];
				ExpireUnit expireUnit = ExpireUnit.valueOf(args[2].toUpperCase());
				long expireTime = Long.parseLong(args[3]);

				GlobalStore.setExpire(key, expireTime*expireUnit.value());
				StringStore.set(key, value);

				ctx.writeAndFlush(toSimpleStringMessage(Reply.OK));
			} else if (args.length == 5) {
				String key = args[0];
				String value = args[1];
				if (isNumeric(args[3])) {
					// SET KEY VALUE EX/PX EXPIRE_TIME NX/XX
					ExpireUnit expireUnit = ExpireUnit.valueOf(args[2].toUpperCase());
					long expireTime = Long.parseLong(args[3]);
					Condition condition = Condition.valueOf(args[4].toUpperCase());

					String oldValue = StringStore.get(key);
					if (condition == Condition.NX && oldValue != null || condition == Condition.XX && oldValue == null) {
						ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
						return;
					}
					GlobalStore.setExpire(key, expireTime*expireUnit.value());
					StringStore.set(key, value);

					ctx.writeAndFlush(toSimpleStringMessage(Reply.OK));
				} else if (isNumeric(args[4])) {
					// SET KEY VALUE NX/XX EX/PX EXPIRE_TIME
					Condition condition = Condition.valueOf(args[2].toUpperCase());
					ExpireUnit expireUnit = ExpireUnit.valueOf(args[3].toUpperCase());
					long expireTime = Long.parseLong(args[4]);

					String oldValue = StringStore.get(key);
					if (condition == Condition.NX && oldValue != null || condition == Condition.XX && oldValue == null) {
						ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
						return;
					}
					GlobalStore.setExpire(key, expireTime*expireUnit.value());
					StringStore.set(key, value);

					ctx.writeAndFlush(toSimpleStringMessage(Reply.OK));
				} else {
					throw new IllegalArgumentException("set命令参数错误");
				}
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

	private enum Condition {
		NX,XX
	}

	private boolean isNumeric(String arg) {
		try {
			Long.parseLong(arg);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
