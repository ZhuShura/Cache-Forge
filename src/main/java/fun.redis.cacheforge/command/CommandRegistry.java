package command;


import command.handler.CommandHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 命令注册器 ———— 在启动时注册
 * @author huangtaiji
 * @date 2025/10/26
 */
public class CommandRegistry {
	private static final Map<String, CommandHandler> handlers = new HashMap<>();

	/**
	 * 注册命令处理器
	 * @param name 命令名称
	 * @param handler 命令处理器
	 */
	public static void register(String name, CommandHandler handler) {
		handlers.put(name.toUpperCase(), handler);
	}

	/**
	 * 获取命令处理器
	 * @param name 命令名称
	 * @return 命令处理器
	 */
	public static CommandHandler get(String name) {
		return handlers.get(name.toUpperCase());
	}
}
