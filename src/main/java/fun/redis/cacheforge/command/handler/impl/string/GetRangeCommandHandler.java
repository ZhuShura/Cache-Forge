package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.storage.repo.StringStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * getrange命令处理器
 * @author huangtaiji
 * @date 2025/10/28
 */
@Slf4j
public class GetRangeCommandHandler implements ReadCommandHandler {

	@Override
	public void handle(ChannelHandlerContext ctx, Command command) {
		try {
			String[] args = command.getArgs();
			if (args.length == 3) {
				String key = args[0];
				int start = Integer.parseInt(args[1]);
				int end = Integer.parseInt(args[2]);

				String value = StringStore.get(key);
				/**
				 * Redis官方是返回空字符串，这里改为返回nil
				 */
				if (value == null) {
					ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
					return;
				}
				start = transformIntoWithinTheRange(start, value.length());
				end = transformIntoWithinTheRange(end, value.length());
				StringBuffer sb = new StringBuffer(value);
				String result = sb.substring(start, end + 1);
				ctx.writeAndFlush(toFullBulkStringMessage(result));
			} else {
				log.error("getrange命令参数错误");
				// todo
				ctx.writeAndFlush(toErrorMessage(Err.ERR));
			}
		} catch (Exception e) {
			log.error("getrange命令异常{}", String.valueOf(e));
			// todo
			ctx.writeAndFlush(toErrorMessage(Err.ERR));
		}
	}


	private int transformIntoWithinTheRange (int index, int range) {
		if (index < 0) index = range + index;
		if (index < 0) index = 0;
		if (index > range) index = range - 1;
		return index;
	}
}
