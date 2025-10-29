package fun.redis.cacheforge.handler;

import fun.redis.cacheforge.command.CommandRegistry;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.Message;
import fun.redis.cacheforge.protocol.model.array.ArrayMessage;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.ErrorMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.IntegerMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.SimpleStringMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import static fun.redis.cacheforge.utils.MessageUtil.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 消息处理
 * @author hua
 * @date 2025/10/27
 */
@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<Message> {



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof ArrayMessage aMsg) {
            List<Message> children = aMsg.children();
            if (children == null || children.isEmpty()) {
                log.error("命令为空");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
                return;
            }
            Message firstChild = children.get(0);
            if (firstChild instanceof FullBulkStringMessage) {
                String commandName = getStringFromBulkMessage((FullBulkStringMessage) firstChild);
                if (commandName == null || CommandRegistry.get(commandName) == null) {
                    log.error("{}: 命令不存在", commandName);
                    ctx.writeAndFlush(toErrorMessage(Err.ERR));
                    return;
                }
                Command command = toCommand(children);
                log.info("---------command start---------");
                log.info("命令：{}，参数：{}", command.getName(), command.getArgs());
                CommandRegistry.get(commandName).handle(ctx, command);
                log.info("----------command end----------");
            }
        } else {
            log.info("111");
            handleSimpleMessage(msg);
        }
    }



    private void handleSimpleMessage(Message msg) {
        if (msg instanceof ErrorMessage) {
            log.error(((ErrorMessage) msg).content());
        } else if (msg instanceof SimpleStringMessage) {
            log.info("Simple string: {}", ((SimpleStringMessage) msg).content());
        } else if (msg instanceof IntegerMessage) {
            log.info("Integer: {}", ((IntegerMessage) msg).value());
        } else if (msg instanceof FullBulkStringMessage) {
            log.info("Full bulk string: {}", msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(String.valueOf(cause));
        ctx.close();
    }
}