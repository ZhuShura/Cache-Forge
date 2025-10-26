package fun.redis.cacheforge.command.handler;

/**
 * 读取命令处理接口
 * @author huangtaiji
 * @date 2025/10/26
 */
public interface ReadCommandHandler extends CommandHandler {
	@Override
	default boolean isWriteCommand() {
		return false;
	}
}
