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
 * incrby 命令处理器
 * @author hua
 * @date 2025/10/27
 */
@Slf4j
public class IncrByCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(Command command, ChannelHandlerContext ctx) {
        try {
            String key = command.getArgs()[0];
            String incrementStr = command.getArgs()[1];
            
            HandleUtil.checkKey(key);
            
            int increment = Integer.parseInt(incrementStr);
            ctx.writeAndFlush(new IntegerMessage(StringStore.incrBy(key, increment)));
        } catch (NumberFormatException e) {
            log.error("incrby命令异常: {}", e.getMessage());
            ctx.writeAndFlush(new ErrorMessage("ERR value is not an integer or out of range"));
        } catch (Exception e) {
            log.error("incrby命令异常: {}", e.getMessage());
            ctx.writeAndFlush(new ErrorMessage(HandleUtil.ERROR));
        }
    }
}