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

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 消息处理
 * @author hua
 * @date 2025/10/27
 */
@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<Message> {

    /**
     * 从 FullBulkStringMessage 中把命令转化成字符串
     *
     * @param bulkMsg 二进制字符串
     * @return 命令的字符串形式
     */
    private static String getStringFromBulkMessage(FullBulkStringMessage bulkMsg) {
        ByteBuf content = bulkMsg.content(); // 获取二进制内容
        if (content == null || !content.isReadable()) {
            return null;
        }
        // Redis 协议中字符串通常用 UTF-8 编码
        return content.toString(StandardCharsets.UTF_8);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof ArrayMessage) {
            handleArrayMessage(ctx, (ArrayMessage) msg);
        } else {
            log.info("111");
            handleSimpleMessage(msg);
        }
    }

    private void handleArrayMessage(ChannelHandlerContext ctx, ArrayMessage msg) {
        List<Message> children = msg.children();
        if (children == null || children.isEmpty()) {
            ctx.writeAndFlush(new ErrorMessage("命令为空"));
            return;
        }
        Message firstChild = children.get(0);
        if (!(firstChild instanceof FullBulkStringMessage)) {
            log.error("命令名格式错误");
            return;
        }
        String commandName = getStringFromBulkMessage((FullBulkStringMessage) firstChild);
        if (commandName == null || CommandRegistry.get(commandName) == null) {
            log.error("{}: 命令不存在", commandName);
            return;
        }

        String[] args = new String[children.size() - 1];
        for (int i = 1; i < children.size(); i++) {
            Message argMessage = children.get(i);
            if (!(argMessage instanceof FullBulkStringMessage)) {
                args[i - 1] = null; // 非字符串参数视为 null
                continue;
            }
            args[i - 1] = getStringFromBulkMessage((FullBulkStringMessage) argMessage);
        }

        // 输出解析结果
        log.info("命令和参数 {} {}", commandName, args.length == 0 ? "" : args);

        // 执行命令
        CommandRegistry.get(commandName).handle(new Command(commandName, args), ctx);
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