package fun.redis.cacheforge.command.handler.impl.zset;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.storage.model.ZSetValue;
import fun.redis.cacheforge.storage.repo.ZSetStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentSkipListSet;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * ZSet计数命令处理器
 * @author hua
 * @date 2025/10/30
 */
@Slf4j
public class ZCountCommandHandler implements ReadCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            if (args.length != 3) {
                log.error("zcount命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
                return;
            }

            String key = args[0];
            ConcurrentSkipListSet<ZSetValue> values = ZSetStore.get(key);
            
            if (values == null || values.isEmpty()) {
                ctx.writeAndFlush(toIntegerMessage(0));
                return;
            }

            String minArg = args[1];
            String maxArg = args[2];
            
            // 解析min和max参数
            double minScore;
            boolean minInclusive = true;
            if (minArg.startsWith("(")) {
                minInclusive = false;
                minScore = Double.parseDouble(minArg.substring(1));
            } else if (minArg.equals("-inf")) {
                minScore = Double.NEGATIVE_INFINITY;
            } else if (minArg.equals("+inf")) {
                minScore = Double.POSITIVE_INFINITY;
            } else {
                minScore = Double.parseDouble(minArg);
            }
            
            double maxScore;
            boolean maxInclusive = true;
            if (maxArg.startsWith("(")) {
                maxInclusive = false;
                maxScore = Double.parseDouble(maxArg.substring(1));
            } else if (maxArg.equals("-inf")) {
                maxScore = Double.NEGATIVE_INFINITY;
            } else if (maxArg.equals("+inf")) {
                maxScore = Double.POSITIVE_INFINITY;
            } else {
                maxScore = Double.parseDouble(maxArg);
            }
            
            // 计算范围内的元素数量
            int count = 0;
            for (ZSetValue value : values) {
                double score = value.getScore();
                boolean inMinRange = minInclusive ? score >= minScore : score > minScore;
                boolean inMaxRange = maxInclusive ? score <= maxScore : score < maxScore;
                
                if (inMinRange && inMaxRange) {
                    count++;
                }
            }
            log.info("服务器返回: {}", count);
            ctx.writeAndFlush(toIntegerMessage(count));
        } catch (Exception e) {
            log.error("zcount命令异常{}", String.valueOf(e));
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }
}
