package fun.redis.cacheforge.command.handler.impl.list;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.repo.ListStore;
import fun.redis.cacheforge.utils.MessageUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fun.redis.cacheforge.utils.MessageUtil.*;


/**
 * lpush命令处理器
 * @author hua
 * @date 2025/10/28
 */
@Slf4j
public class LPushCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args != null && args.length > 1) {
                String key = args[0];
                List<String> values = Arrays.stream(args, 1, args.length).toList();
                List<String> list = ListStore.get(key);
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.addAll(0, values);
                ListStore.set(key, list);
                ctx.writeAndFlush(toIntegerMessage(list.size()));
                log.info("服务器返回: {}", list.size());
            } else {
                log.error("lpush命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("lpush命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }
}
