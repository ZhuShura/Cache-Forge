package fun.redis.cacheforge.command.handler.impl.string;


import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.generalizedInline.ErrorMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.SimpleStringMessage;
import fun.redis.cacheforge.storage.repo.StringStore;
import fun.redis.cacheforge.utils.HandleUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * set命令处理器
 * @author hua
 * @date 2025/10/27
 */
@Slf4j
public class SetCommandHandler implements WriteCommandHandler {
	@Override
	public void handle(Command command, ChannelHandlerContext ctx) {
		try {
			String key = command.getArgs()[0];
			String value = command.getArgs()[1];
			StringStore.set(key, value);
			ctx.writeAndFlush(new SimpleStringMessage(HandleUtil.OK));
		} catch (Exception e) {
            log.error("set命令异常{}", String.valueOf(e));
			ctx.writeAndFlush(new ErrorMessage(HandleUtil.ERROR));
		}
	}
}
