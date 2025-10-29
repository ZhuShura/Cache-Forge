package fun.redis.cacheforge.command.handler.impl.set;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.storage.repo.SetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static fun.redis.cacheforge.utils.MessageUtil.*;

@Slf4j
public class SPopCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length >= 1) {
                String key = args[0];
                int count = 1;
                if (args.length == 2) {
                    count = Integer.parseInt(args[1]);
                } else if (args.length > 2) {
                    log.error("scard命令参数数量错误");
                    ctx.writeAndFlush(toErrorMessage(Err.ERR));
                }
                Set<String> set = SetStore.get(key);
                if (set == null) {
                    log.info("无此秘钥, 服务器返回: null");
                    ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
                    return;
                }
                List<String> result = new ArrayList<>(set);
                Collections.shuffle(result);
                result = result.subList(0, Math.min(count, result.size()));
                result.forEach(set::remove);
                SetStore.set(key, set);
                log.info("服务器返回: {}", result);
                if (args.length == 2) {
                    ctx.writeAndFlush(basicToArrayMessage(result));
                } else {
                    ctx.writeAndFlush(toFullBulkStringMessage(result.get(0)));
                }
            } else {
                log.error("scard命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("scard命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }
}
