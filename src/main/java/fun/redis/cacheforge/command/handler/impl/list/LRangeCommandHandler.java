package fun.redis.cacheforge.command.handler.impl.list;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.storage.repo.ListStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * lrange命令处理器
 * @author hua
 * @date 2025/10/28
 */
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
                    log.error("key不存在");
                    ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
                    return;
                }
                // 处理索引
                start = start < 0 ? list.size() + start : start;
                end = end < 0 ? list.size() + end : end;
                // 处理超出范围的情况
                if (start >= list.size() || end < 0) {
                    log.error("索引超出范围");
                    ctx.writeAndFlush(toErrorMessage(Err.ERR_IDX));
                    return;
                }
                // 提取子列表
                List<String> range = list.subList(start, end + 1);
                ctx.writeAndFlush(toArrayMessage(range));
                log.info("服务器返回: {}", range);
            } else {
                log.error("lrange命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (IndexOutOfBoundsException e) {
          log.error("索引超出范围", e);
          ctx.writeAndFlush(toErrorMessage(Err.ERR_IDX));
        } catch (Exception e) {
            log.error("lrange命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }
}
