package command.handler;

/**
 * 写命令处理接口
 * @author huangtaiji
 * @date 2025/10/26
 */
public interface WriteCommandHandler extends CommandHandler{
	@Override
	default boolean isWriteCommand() {
		return true;
	}
}
