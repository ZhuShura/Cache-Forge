package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.repo.StringStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * setrange命令处理器
 * @author huangtaiji
 * @date 2025/10/28
 */
@Slf4j
public class SetRangeCommandHandler implements WriteCommandHandler {

	@Override
	public void handle(ChannelHandlerContext ctx, Command command) {
		try {
			String[] args = command.getArgs();
			if (args.length == 3) {
				String key = args[0];
				int offset = Integer.parseInt(args[1]);
				String value = args[2];

				String oldValue = StringStore.get(key);
				StringBuilder sb = oldValue == null ? new StringBuilder() : new StringBuilder(oldValue);
				while (offset > sb.length()) {
					sb.append(" ");
				}
				for (int i = 0; i < value.length(); i++) {
					if (offset + i < sb.length()) {
						sb.setCharAt(offset + i, value.charAt(i));
					} else {
						sb.append(value.charAt(i));
					}
				}
				StringStore.set(key, sb.toString());
				ctx.writeAndFlush(toIntegerMessage(sb.length()));
			} else {
				log.error("setrange命令参数错误");
				// todo
				ctx.writeAndFlush(toErrorMessage(Err.ERR));
			}
		} catch (Exception e) {
			log.error("setrange命令异常{}", String.valueOf(e));
			// todo
			ctx.writeAndFlush(toErrorMessage(Err.ERR));
		}
	}
}
