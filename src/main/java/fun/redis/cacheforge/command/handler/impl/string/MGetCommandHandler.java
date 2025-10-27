package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.Message;
import fun.redis.cacheforge.protocol.model.array.ArrayMessage;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.storage.repo.StringStore;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
            
            // 批量获取值
            List<String> values = StringStore.mGet(keys);
            
            // 将结果转换为消息列表
            List<Message> messages = new ArrayList<>();
            for (String value : values) {
                messages.add(new FullBulkStringMessage(Unpooled.wrappedBuffer(value.getBytes(StandardCharsets.UTF_8))));
            }
            
            // 返回数组消息
            ctx.writeAndFlush(new ArrayMessage(messages));
        } catch (Exception e) {
            log.error("mget命令执行异常: {}", e.getMessage(), e);
            ctx.writeAndFlush(new ArrayMessage(List.of()));
        }
    }
}