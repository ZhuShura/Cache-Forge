package fun.redis.cacheforge.command.handler.impl.set;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.array.ArrayMessage;
import fun.redis.cacheforge.storage.repo.SetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * sinter命令处理器
 * @author hua
 * @date 2025/10/29
 */
@Slf4j
public class SInterCommandHandler implements ReadCommandHandler {

    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length >= 1) {
                List<String> keys = List.of(args);
                List<Set<String>> sets = new ArrayList<>();
                for (String key : keys) {
                    Set<String> set = SetStore.get(key);
                    if (set == null) {
                        ctx.writeAndFlush(ArrayMessage.EMPTY_INSTANCE);
                        return;
                    }
                    sets.add(set);
                }
                sets.sort(Comparator.comparingInt(Set::size));
                Set<String> smallestSet = sets.get(0);
                for (Set<String> set : sets) {
                    smallestSet.retainAll(set);
                }
                log.info("服务器返回: {}", smallestSet);
                ctx.writeAndFlush(basicToArrayMessage(Arrays.asList(smallestSet.toArray())));
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
