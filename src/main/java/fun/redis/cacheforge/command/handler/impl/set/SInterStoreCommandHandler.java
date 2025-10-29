package fun.redis.cacheforge.command.handler.impl.set;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.repo.SetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static fun.redis.cacheforge.utils.MessageUtil.*;

@Slf4j
public class SInterStoreCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length >= 2) {
                String destination = args[0];
                List<String> keys = List.of(args).subList(1, args.length);
                List<Set<String>> sets = new ArrayList<>();
                for (String key : keys) {
                    Set<String> set = SetStore.get(key);
                    if (set == null) {
                        ctx.writeAndFlush(toIntegerMessage(0));
                        return;
                    }
                    sets.add(set);
                }
                sets.sort(Comparator.comparingInt(Set::size));
                Set<String> smallestSet = sets.get(0);
                for (Set<String> set : sets) {
                    smallestSet.retainAll(set);
                }
                SetStore.set(destination, smallestSet);
                log.info("服务器返回: {}", smallestSet.size());
                ctx.writeAndFlush(toIntegerMessage(smallestSet.size()));
            } else {
                log.error("sinter命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("sinter命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }
}
