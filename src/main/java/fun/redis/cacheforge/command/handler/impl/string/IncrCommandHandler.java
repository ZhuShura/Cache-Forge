package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.common.CacheForgeCodecException;
import fun.redis.cacheforge.storage.repo.StringStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import static fun.redis.cacheforge.utils.MessageUtil.*;


/**
 * incr命令处理器
 * @author huangtaiji
 * @date 2025/10/28
 */
@Slf4j
public class IncrCommandHandler implements WriteCommandHandler {
	@Override
	public void handle(ChannelHandlerContext ctx, Command command) {
		try {
			String[] args = command.getArgs();
			if (args.length == 1) {
				String key = args[0];
				String value = StringStore.get(key);
				int result;
				if (value == null) {
					result = 1;
				} else if (value.isEmpty()) {
					throw new CacheForgeCodecException("Redis官方认为有问题");
				} else {
					result = Integer.parseInt(value) + 1;
				}
				StringStore.set(key, String.valueOf(result));
				ctx.writeAndFlush(toIntegerMessage(result));
			} else {
				log.error("incr命令参数错误");
				// todo
				ctx.writeAndFlush(toErrorMessage(Err.ERR));
			}
		} catch (Exception e) {
			log.error("incr命令异常{}", String.valueOf(e));
			// todo
			ctx.writeAndFlush(toErrorMessage(Err.ERR));
		}
	}
}
