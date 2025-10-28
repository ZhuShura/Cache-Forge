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
 * linsert命令处理器
 * @author hua
 * @date 2025/10/28
 */
@Slf4j
public class LInsertCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length == 4) {
                String key = args[0];
                Position position = Position.valueOf(args[1].toUpperCase());
                String pivot = args[2];
                String value = args[3];
                List<String> list = ListStore.get(key);
                if (list == null) {
                    log.error("key不存在");
                    ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
                    return;
                }
                int index = list.indexOf(pivot);
                // 验证pivot是否存在
                if (index == -1) {
                    log.info("pivot不存在");
                    ctx.writeAndFlush(toIntegerMessage(index));
                    return;
                }
                if (position == Position.BEFORE) {
                    list.add(index, value);
                } else if (position == Position.AFTER) {
                    list.add(index + 1, value);
                } else {
                    log.error("linsert命令参数错误");
                    ctx.writeAndFlush(toErrorMessage(Err.ERR));
                    return;
                }
                ctx.writeAndFlush(toIntegerMessage(list.size()));
                log.info("服务器返回: {}", list.size());
            } else {
                log.error("linsert命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("linsert命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }

    private enum Position {
        BEFORE, AFTER
    }
}
