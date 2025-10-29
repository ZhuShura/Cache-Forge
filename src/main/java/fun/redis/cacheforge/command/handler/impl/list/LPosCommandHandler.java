package fun.redis.cacheforge.command.handler.impl.list;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.protocol.model.array.ArrayMessage;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.storage.repo.ListStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static fun.redis.cacheforge.utils.MessageUtil.*;

/**
 * lpos命令处理器
 *
 * @author hua
 * @date 2025/10/28
 */
@Slf4j
public class LPosCommandHandler implements ReadCommandHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, Command command) {
        try {
            String[] args = command.getArgs();
            // 命令长度为2, 只用返回找到的第一个元素索引
            if (args.length == 2) {
                String key = args[0];
                String element = args[1];
                List<String> list = ListStore.get(key);
                if (list == null) {
                    log.error("key不存在");
                    ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
                    return;
                }
                int index = list.indexOf(element);
                ctx.writeAndFlush(toIntegerMessage(index));
                log.info("服务器返回: {}", list.size());
            }
            // 命令长度为4, 需要判断命令为rank还是count
            else if (args.length == 4) {
                String key = args[0];
                String element = args[1];
                Condition condition = Condition.valueOf(args[2].toUpperCase());
                int num = Integer.parseInt(args[3]);
                List<String> list = ListStore.get(key);
                if (list == null) {
                    log.error("key不存在");
                    ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
                    return;
                }
                // rank命令, 返回第rank个元素索引
                if (condition == Condition.RANK) {
                    int index = findIndexByRank(list, element, num);
                    if (index == -2) {
                        log.error("rank不能为0");
                        ctx.writeAndFlush(toErrorMessage(Err.ERR));
                    } else if (index == -1) {
                        log.info("服务器返回: null");
                        ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
                    } else {
                        log.info("服务器返回: {}", index);
                        ctx.writeAndFlush(toIntegerMessage(index));
                    }
                }
                // count命令, 返回多个元素索引
                else if (condition == Condition.COUNT) {
                    // count小于零, 错误
                    if (num < 0) {
                        log.error("count不能为负数");
                        ctx.writeAndFlush(toErrorMessage(Err.ERR));
                        return;
                    }
                    List<Integer> indexes = findIndexesByCount(list, element, num, 0, 0);
                    log.info("服务器返回: {}", indexes);
                    ctx.writeAndFlush(basicToArrayMessage(indexes));
                } else {
                    log.error("lpos命令参数错误");
                    ctx.writeAndFlush(toErrorMessage(Err.ERR));
                }
            }
            // 命令长度为6, 命令为rank和count的聚合体
            else if (args.length == 6) {
                String key = args[0];
                String element = args[1];
                Condition condition1 = Condition.valueOf(args[2].toUpperCase());
                int num1 = Integer.parseInt(args[3]);
                Condition condition2 = Condition.valueOf(args[4].toUpperCase());
                int num2 = Integer.parseInt(args[5]);
                List<String> list = ListStore.get(key);
                if (list == null) {
                    log.error("key不存在");
                    ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
                    return;
                }
                // 命名count和rank以区分命令参数
                int count = 0, rank = 1;
                if (condition1 == Condition.RANK && condition2 == Condition.COUNT) {
                    count = num2;
                    rank = num1;
                } else if (condition1 == Condition.COUNT && condition2 == Condition.RANK) {
                    count = num1;
                    rank = num2;
                } else {
                    log.error("lpos命令参数错误");
                    ctx.writeAndFlush(toErrorMessage(Err.ERR));
                }
                if (count < 0) {
                    log.error("count不能为负数");
                    ctx.writeAndFlush(toErrorMessage(Err.ERR));
                    return;
                }
                int index = findIndexByRank(list, element, rank);
                if (index == -2) {
                    log.error("rank不能为0");
                    ctx.writeAndFlush(toErrorMessage(Err.ERR));
                } else if (index == -1) {
                    log.info("服务器返回: null");
                    ctx.writeAndFlush(ArrayMessage.EMPTY_INSTANCE);
                } else {
                    List<Integer> indexes = findIndexesByCount(list, element, count, index, rank);
                    log.info("服务器返回: {}", indexes);
                    ctx.writeAndFlush(basicToArrayMessage(indexes));
                }
            } else {
                log.error("lpos命令参数数量错误");
                ctx.writeAndFlush(toErrorMessage(Err.ERR));
            }
        } catch (Exception e) {
            log.error("lpos命令异常", e);
            ctx.writeAndFlush(toErrorMessage(Err.ERR));
        }
    }

    private enum Condition {
        RANK, COUNT
    }

    /**
     * 根据rank查找元素索引
     * @param list 列表数据
     * @param element 查找元素
     * @param rank rank值（正数从头开始，负数从尾开始）
     * @return 元素索引，未找到返回-1
     */
    private int findIndexByRank(List<String> list, String element, int rank) {
        int count = 0;
        int index = -1;

        if (rank == 0) {
            // rank不能为0, 返回-2让前面报错
            return -2;
        }

        if (rank > 0) {
            // 正数从头开始查找
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(element)) {
                    count++;
                    if (count == rank) {
                        index = i;
                        break;
                    }
                }
            }
        } else {
            // 负数从尾开始查找
            for (int i = list.size() - 1; i >= 0; i--) {
                if (list.get(i).equals(element)) {
                    count++;
                    if (count == -rank) {
                        index = i;
                        break;
                    }
                }
            }
        }
        return index;
    }

    /**
     * 根据count查找多个元素索引
     * @param list 列表数据
     * @param element 查找元素
     * @param count 需要返回的索引数量（0表示返回所有）
     * @param index 索引起始位置（默认从0开始）
     * @return 元素索引列表
     */
    private List<Integer> findIndexesByCount(List<String> list, String element, int count, int index, int rank) {
        List<Integer> indexes = new ArrayList<>();

        if (count < 0) {
            return indexes; // count不能为负数
        }

        if (count == 0) {
            // 返回所有匹配元素的索引
            for (int i = index; i < list.size(); i++) {
                if (list.get(i).equals(element)) {
                    indexes.add(i);
                }
            }
        } else if (rank >= 0) {
            // 正数从头开始查找
            for (int i = index; i < list.size(); i++) {
                if (list.get(i).equals(element)) {
                    indexes.add(i);
                    if (indexes.size() == count) {
                        break;
                    }
                }
            }
        } else {
            // 负数从尾开始查找
            for (int i = index; i >= 0; i--) {
                if (list.get(i).equals(element)) {
                    indexes.add(i);
                    if (indexes.size() == count) {
                        break;
                    }
                }
            }
        }

        return indexes;
    }

}
