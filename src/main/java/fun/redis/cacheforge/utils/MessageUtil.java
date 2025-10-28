package fun.redis.cacheforge.utils;

import fun.redis.cacheforge.common.CacheForgeCodecException;
import fun.redis.cacheforge.protocol.model.Message;
import fun.redis.cacheforge.protocol.model.array.ArrayMessage;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.ErrorMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.IntegerMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.SimpleStringMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import fun.redis.cacheforge.command.model.Command;

import static fun.redis.cacheforge.utils.CacheForgeCodecUtil.longToAsciiBytes;

/**
 * 消息工具类
 * 1.接收{@link Message}转换成{@link Command}
 * 2.封装{@link Message}传给客户端
 * 3.自定义常用Message
 * @author hua
 * @author huangtaiji
 * @date 2025/10/28
 */
public final class MessageUtil {
    // -------------------处理Message——>Command-------------------------

    /**
     * 从 FullBulkStringMessage 中把命令转化成字符串
     *
     * @param bMsg 批量字符串消息
     * @return 消息的字符串形式
     */
    public static String getStringFromBulkMessage(FullBulkStringMessage bMsg) {
        ByteBuf content = bMsg.content();                                      // 获取二进制内容
        if (content == null || !content.isReadable()) {
            return null;
        }
        return content.toString(StandardCharsets.UTF_8);
    }

    /**
     * 从ArrayMessage中把命令转化成Command
     *
     * @param children ArrayMessage的子消息
     * @return 命令
     */
    public static Command toCommand(List<Message> children) {
        String commandName = getStringFromBulkMessage((FullBulkStringMessage) children.get(0));
        String[] args = new String[children.size() - 1];
        for (int i = 1; i < children.size(); i++) {
            Message argMessage = children.get(i);
            if (!(argMessage instanceof FullBulkStringMessage)) {
                args[i - 1] = null;                                         // 非字符串参数视为 null
                continue;
            }
            args[i - 1] = getStringFromBulkMessage((FullBulkStringMessage) argMessage);
        }
        return new Command(commandName, args);
    }


    // -------------------封装Message--------------------------

    /**
     * 封装成简单字符串消息
     * @param reply 常用回复
     * @return  简单字符串消息
     */
    public static SimpleStringMessage toSimpleStringMessage(Reply reply) {
        return new SimpleStringMessage(reply.value());
    }

    /**
     * 封装成错误消息
     * @param error 常见错误
     * @return 错误消息
     */
    public static ErrorMessage toErrorMessage(Err error) {
        return new ErrorMessage(error.value());
    }

    /**
     * 封装成整数消息
     * @param value 值
     * @return 整数消息
     */
    public static IntegerMessage toIntegerMessage(long value) {
        return new IntegerMessage(value);
    }

    /**
     * 将字符串转换为FullBulkStringMessage
     * @param value 值
     * @return FullBulkStringMessage
     */
    public static FullBulkStringMessage toFullBulkStringMessage(String value) {
        return new FullBulkStringMessage(Unpooled.wrappedBuffer(value.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 将整数转换为FullBulkStringMessage
     * @param value 值
     * @return FullBulkStringMessage
     */
    @Deprecated
    public static FullBulkStringMessage toFullBulkStringMessage(Integer value) {
        return new FullBulkStringMessage(Unpooled.wrappedBuffer(longToAsciiBytes(value)));
    }

    /**
     * 将列表转换为ArrayMessage
     * @param values 值列表
     * @return ArrayMessage
     */
    public static <T> ArrayMessage toArrayMessage(List<T> values) {
        List<Message> messages = new ArrayList<>();
        for (T value : values) {
            if (value instanceof String v) {
                messages.add(toFullBulkStringMessage(v));
            } else if (value instanceof Integer v) {
                messages.add(toIntegerMessage(v));
            } else {
                throw new CacheForgeCodecException("不支持的类型");
            }
        }
        return new ArrayMessage(messages);
    }


    // -------------------缓存常用Message--------------------------------
    public enum Reply {
        OK("OK"),
        PONG("PONG"),
        QUEUED("QUEUED");

        private final String value;

        Reply(String value) {
            this.value = value;
        }

        private String value() {
            return value;
        }
    }

    public enum Err {
        ERR("ERR"),
        ERR_IDX("ERR index out of range"),
        ERR_NOKEY("ERR no such key"),
        ERR_SAMEOBJ("ERR source and destination objects are the same"),
        ERR_SYNTAX("ERR syntax error"),
        BUSY("BUSY Redis is busy running a script. You can only call SCRIPT KILL or SHUTDOWN NOSAVE."),
        BUSYKEY("BUSYKEY Target key name already exists."),
        EXECABORT("EXECABORT Transaction discarded because of previous errors."),
        LOADING("LOADING Redis is loading the dataset in memory"),
        MASTERDOWN("MASTERDOWN Link with MASTER is down and slave-serve-stale-data is set to 'no'."),
        MISCONF("MISCONF Redis is configured to save RDB snapshots, but is currently not able to persist on disk. " +
                "Commands that may modify the data set are disabled. Please check Redis logs for details " +
                "about the error."),
        NOREPLICAS("NOREPLICAS Not enough good slaves to write."),
        NOSCRIPT("NOSCRIPT No matching script. Please use EVAL."),
        OOM("OOM command not allowed when used memory > 'maxmemory'."),
        READONLY("READONLY You can't write against a read only slave."),
        WRONGTYPE("WRONGTYPE Operation against a key holding the wrong kind of value"),
        NOT_AUTH("NOAUTH Authentication required.");

        private final String value;

        Err(String value) {
            this.value = value;
        }

        private String value() {
            return value;
        }
    }




}
