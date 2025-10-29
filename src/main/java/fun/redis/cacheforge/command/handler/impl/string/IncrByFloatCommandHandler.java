package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.common.CacheForgeCodecException;
import fun.redis.cacheforge.storage.repo.StringStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * incrbyfloat命令处理器 todo 待实现
 * @author huangtaiji
 * @date 2025/10/28
 */
@Slf4j
public class IncrByFloatCommandHandler implements WriteCommandHandler {
	@Override
	public void handle(ChannelHandlerContext ctx, Command command) {
		String[] args = command.getArgs();
		try {
			if (command.getArgs().length == 2) {
				String key = args[0];
				BigDecimal increment = BigDecimal.valueOf(Double.parseDouble(args[1]));

				String value = StringStore.get(key);
				BigDecimal result;
				if (value == null) {
					result = increment;
				} else if (value.isEmpty()) {
					throw new CacheForgeCodecException("Redis官方认为有问题");
				} else {
					result = new BigDecimal(value).add(increment);
				}
				StringStore.set(key, result.toString());
				ctx.writeAndFlush(toFullBulkStringMessage(result.toString()));
			} else {
				log.error("incrbyfloat命令参数错误");
				// todo
				ctx.writeAndFlush(toErrorMessage(Err.ERR));
			}
		} catch (Exception e) {
			log.error("incrbyfloat命令异常{}", String.valueOf(e));
			// todo
			ctx.writeAndFlush(toErrorMessage(Err.ERR));
		}
	}
}
