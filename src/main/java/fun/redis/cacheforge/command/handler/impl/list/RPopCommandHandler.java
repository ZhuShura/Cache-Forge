package fun.redis.cacheforge.command.handler.impl.list;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.repo.ListStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * rpop命令处理器
 * @author hua
 * @date 2025/10/28
 */
@Slf4j
public class RPopCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length == 2) {
                String key = args[0];
                int count = Integer.parseInt(args[1]);
                List<String> list = ListStore.get(key);
                List<String> removed = new ArrayList<>();
                while (count -- > 0 && list != null && !list.isEmpty()) {
                    removed.add(list.remove(list.size() - 1));
                }
                ListStore.set(key, list);
                ctx.writeAndFlush(basicToArrayMessage(removed));
                log.info("服务器返回: {}", removed);
            } else {
                log.error("rpop命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("rpop命令异常{}", String.valueOf(e));
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }
}