package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.generalizedInline.ErrorMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.SimpleStringMessage;
import fun.redis.cacheforge.storage.repo.StringStore;
import fun.redis.cacheforge.utils.HandleUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * mset命令处理器
 * @author hua
 * @date 2025/10/27
 */
@Slf4j
public class MSetCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(Command command, ChannelHandlerContext ctx) {
        try {
            String[] args = command.getArgs();
            // MSET命令需要偶数个参数（键值对）
            if (args.length == 0 || args.length % 2 != 0) {
                ctx.writeAndFlush(new ErrorMessage("ERR wrong number of arguments for 'mset' command"));
                return;
            }
            
            // 构造键值对映射
            Map<String, String> keyValuePairs = new HashMap<>();
            for (int i = 0; i < args.length; i += 2) {
                String key = args[i];
                String value = args[i + 1];
                keyValuePairs.put(key, value);
            }
            
            // 批量设置键值对
            StringStore.mSet(keyValuePairs);
            ctx.writeAndFlush(new SimpleStringMessage(HandleUtil.OK));
        } catch (Exception e) {
            log.error("mset命令执行异常: {}", e.getMessage(), e);
            ctx.writeAndFlush(new ErrorMessage("ERR mset command failed"));
        }
    }
}