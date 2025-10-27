package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.generalizedInline.ErrorMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.IntegerMessage;
import fun.redis.cacheforge.storage.repo.StringStore;
import fun.redis.cacheforge.utils.HandleUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * incr命令处理器
 * @author hua
 * @date 2025/10/27
 */
@Slf4j
public class IncrCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(Command command, ChannelHandlerContext ctx) {
        try {
            String key = command.getArgs()[0];
            HandleUtil.checkKey(key);
            ctx.writeAndFlush(new IntegerMessage(StringStore.incr(key)));
        } catch (NumberFormatException e) {
            log.error("decrby命令异常: {}", e.getMessage());
            ctx.writeAndFlush(new ErrorMessage("ERR value is not an integer or out of range"));
        } catch (Exception e) {
            log.error("decrby命令异常: {}", e.getMessage());
            ctx.writeAndFlush(new ErrorMessage(HandleUtil.ERROR));
        }
    }
}
