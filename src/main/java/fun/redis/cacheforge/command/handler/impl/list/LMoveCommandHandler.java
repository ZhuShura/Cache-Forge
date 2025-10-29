package fun.redis.cacheforge.command.handler.impl.list;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.storage.repo.ListStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * lmove命令处理器
 * @author hua
 * @date 2025/10/28
 */
@Slf4j
public class LMoveCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length == 4) {
                String source = args[0];
                String destination = args[1];
                Direction sourceDirection = Direction.valueOf(args[2].toUpperCase());
                Direction destinationDirection = Direction.valueOf(args[3].toUpperCase());
                List<String> sourceList = ListStore.get(source);
                List<String> destinationList = ListStore.get(destination);
                if (sourceList == null) {
                    log.error("源列表不存在");
                    ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
                    return;
                }
                if (destinationList == null) {
                    destinationList = new ArrayList<>();
                }
                String remove = sourceList.remove(sourceDirection == Direction.LEFT ? 0 : sourceList.size() - 1);
                destinationList.add(destinationDirection == Direction.LEFT ? 0 : destinationList.size(), remove);
                ListStore.set(destination, destinationList);
                ListStore.set(source, sourceList);
                ctx.writeAndFlush(toFullBulkStringMessage(remove));
                log.info("服务器返回: {}", remove);
            } else {
                log.error("lmove命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("lmove命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }

    private enum Direction {
        LEFT, RIGHT
    }
}
