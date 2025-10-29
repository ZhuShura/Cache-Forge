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
 * ltrim命令处理器
 * @author hua
 * @date 2025/10/28
 */
@Slf4j
public class LTrimCommandHandler implements WriteCommandHandler {
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
                start = start < 0 ? list.size() + start : start;
                end = end < 0 ? list.size() + end : end;
                list = list.subList(start, end + 1);
                ListStore.set(key, list);
                ctx.writeAndFlush(toSimpleStringMessage(Reply.OK));
                log.info("服务器返回: {}", list.size());
            } else {
                log.error("ltrim命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("ltrim命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }
}
