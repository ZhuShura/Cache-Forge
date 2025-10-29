package fun.redis.cacheforge.command.handler.impl.set;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.repo.SetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static fun.redis.cacheforge.utils.MessageUtil.*;

@Slf4j
public class SRemCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length >= 2) {
                String key = args[0];
                Set<String> members = Set.copyOf(List.of(args).subList(1, args.length));
                Set<String> set = SetStore.get(key);
                if (set == null) {
                    set = new HashSet<>();
                }
                int originalSize = set.size();
                set.removeAll(members);
                int result = originalSize - set.size();
                SetStore.set(key, set);
                log.info("服务器返回: {}", result);
                ctx.writeAndFlush(toIntegerMessage(result));
            } else {
                log.error("sadd命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("sadd命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }
}
