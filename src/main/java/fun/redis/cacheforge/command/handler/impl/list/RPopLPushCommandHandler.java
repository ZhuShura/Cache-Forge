package fun.redis.cacheforge.command.handler.impl.list;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.storage.repo.ListStore;
import fun.redis.cacheforge.utils.MessageUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * rpoplpush命令处理器
 * @author hua
 * @date 2025/10/28
 */
@Slf4j
@Deprecated
public class RPopLPushCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length == 2) {
                String source = args[0];
                String destination = args[1];
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
                String remove = sourceList.remove(sourceList.size() - 1);
                destinationList.add(remove);
                ListStore.set(destination, destinationList);
                ListStore.set(source, sourceList);
                ctx.writeAndFlush(toFullBulkStringMessage(remove));
                log.info("服务器返回: {}",remove);
            } else {
                log.error("rpoplpush命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("rpoplpush命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }
}
