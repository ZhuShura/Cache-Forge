package fun.redis.cacheforge.command.handler;


import fun.redis.cacheforge.command.model.Command;

/**
 * 命令处理接口
 * @author huangtaiji
 * @date 2025/10/26
 */
public interface CommandHandler {
	void handle(Command command);
	boolean isWriteCommand();
}
