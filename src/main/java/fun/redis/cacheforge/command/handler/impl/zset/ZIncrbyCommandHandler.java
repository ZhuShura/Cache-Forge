package fun.redis.cacheforge.command.handler.impl.zset;

import fun.redis.cacheforge.command.handler.WriteCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.model.ZSetValue;
import fun.redis.cacheforge.storage.repo.ZSetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

import static fun.redis.cacheforge.utils.MessageUtil.*;

@Slf4j
public class ZIncrbyCommandHandler implements WriteCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length == 3) {
                String key = args[0];
                double increment = Double.parseDouble(args[1]);
                String member = args[2];
                ConcurrentSkipListSet<ZSetValue> set = ZSetStore.get(key);
                Map<String, Double> memberToScore = ZSetStore.getMemberToScoreMap(key);
                if (set == null) {
                    set = new ConcurrentSkipListSet<>();
                }
                if (memberToScore == null) {
                    memberToScore = new HashMap<>();
                }
                Double currentScore = memberToScore.get(member);
                if (currentScore == null) {
                    currentScore = 0.0;
                }
                ZSetValue zSetValue = new ZSetValue(member, currentScore + increment);
                set.remove(new ZSetValue(member, currentScore));
                set.add(zSetValue);
                memberToScore.put(member, zSetValue.getScore());
                ZSetStore.set(key, set, memberToScore);
                log.info("服务器返回: {}", currentScore + increment);
                ctx.writeAndFlush(toFullBulkStringMessage(String.valueOf(currentScore + increment)));
            } else {
                log.error("zincrby命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("zincrby命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }
}
