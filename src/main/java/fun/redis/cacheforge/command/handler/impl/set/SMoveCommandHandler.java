package fun.redis.cacheforge.command.handler.impl.set;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.repo.SetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

import static fun.redis.cacheforge.utils.MessageUtil.*;

@Slf4j
public class SMoveCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length == 3) {
                String source = args[0];
                String destination = args[1];
                String member = args[2];
                Set<String> sourceSet = SetStore.get(source);
                Set<String> destinationSet = SetStore.get(destination);
                if (sourceSet == null) {
                    sourceSet = new HashSet<>();
                }
                if (destinationSet == null) {
                    destinationSet = new HashSet<>();
                }
                int result = 0;
                if (sourceSet.remove(member)) {
                    destinationSet.add(member);
                    SetStore.set(source, sourceSet);
                    SetStore.set(destination, destinationSet);
                    result = 1;
                }
                log.info("服务器返回: {}", result);
                ctx.writeAndFlush(toIntegerMessage(result));
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
