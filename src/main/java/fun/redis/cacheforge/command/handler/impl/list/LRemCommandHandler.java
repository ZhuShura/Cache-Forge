package fun.redis.cacheforge.command.handler.impl.list;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.storage.repo.ListStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * lrem命令处理器
 * @author hua
 * @date 2025/10/28
 */
@Slf4j
public class LRemCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length == 3) {
                String key = args[0];
                int count = Integer.parseInt(args[1]);
                String element = args[2];
                List<String> list = ListStore.get(key);
                if (list == null) {
                    log.error("key不存在");
                    ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
                    return;
                }
                int removeCount = 0;
                if (count > 0) {
                    while (removeCount < count) {
                        int index = list.indexOf(element);
                        if (index == -1) {
                            break;
                        }
                        list.remove(index);
                        removeCount++;
                    }
                } else if (count < 0) {
                    while (removeCount < -count) {
                        int index = list.lastIndexOf(element);
                        if (index == -1) {
                            break;
                        }
                        list.remove(index);
                        removeCount++;
                    }
                } else {
                    while (list.remove(element)) {
                        removeCount++;
                    }
                }
                ListStore.set(key, list);
                ctx.writeAndFlush(toIntegerMessage(removeCount));
                log.info("服务器返回: {}", removeCount);
            } else {
                log.error("lrem命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("lrem命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }
}
