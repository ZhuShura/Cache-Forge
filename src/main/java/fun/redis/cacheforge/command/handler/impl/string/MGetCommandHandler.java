package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.array.ArrayMessage;
import fun.redis.cacheforge.storage.repo.StringStore;
import fun.redis.cacheforge.utils.HandleUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * mget命令处理器
 * @author hua
 * @date 2025/10/27
 */
@Slf4j
public class MGetCommandHandler implements ReadCommandHandler {
    @Override
    public void handle(Command command, ChannelHandlerContext ctx) {
        try {
            String[] args = command.getArgs();
            if (args.length == 0) {
                // 如果没有参数，返回空数组
                ctx.writeAndFlush(ArrayMessage.EMPTY_INSTANCE);
                return;
            }

            // 将参数转换为List<String>
            List<String> keys = List.of(args);

            // 返回数组消息
            ctx.writeAndFlush(HandleUtil.toArrayMessage(StringStore.mGet(keys)));
        } catch (Exception e) {
            log.error("mget命令执行异常: {}", e.getMessage(), e);
            ctx.writeAndFlush(new ArrayMessage(List.of()));
        }
    }
}