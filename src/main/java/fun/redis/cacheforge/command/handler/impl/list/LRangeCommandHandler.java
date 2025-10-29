package fun.redis.cacheforge.command.handler.impl.list;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.storage.repo.ListStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static fun.redis.cacheforge.utils.MessageUtil.*;

@Slf4j
public class LRangeCommandHandler implements ReadCommandHandler {

    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length == 3) {
                String key = args[0];
                int start = Integer.parseInt(args[1]);
                int end = Integer.parseInt(args[2]);
                List<String> list = ListStore.get(key);
                if (list == null) {
                    log.info("key不存在");
                    ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
                    return;
                }
                start = start >= 0 ? start : list.size() + start;
                end = end >= 0 ? end : list.size() + end;
                List<String> range = list.subList(start, end + 1);
                ctx.writeAndFlush(basicToArrayMessage(range));
                log.info("服务器返回: {}", range);
            } else {
                log.error("lrange命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (IndexOutOfBoundsException e) {
          log.error("lrange命令导致越界", e);
          ctx.writeAndFlush(toErrorMessage(Err.ERR_IDX));
        } catch (Exception e) {
            log.error("lrange命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }
}
