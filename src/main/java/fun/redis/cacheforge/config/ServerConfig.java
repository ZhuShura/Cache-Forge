package fun.redis.cacheforge.config;

import fun.redis.cacheforge.command.CommandRegistry;
import fun.redis.cacheforge.command.handler.CommandHandler;
import fun.redis.cacheforge.command.handler.impl.CommandCommandHandler;
import fun.redis.cacheforge.command.handler.impl.string.*;

public class ServerConfig {
    public final int PORT = 6379;

    public ServerConfig() {
        // ---------------------Normal-------------------
        CommandRegistry.register("command", new CommandCommandHandler());
        CommandRegistry.register("del", new DelCommandHandler());
        // ---------------------String-------------------
        CommandRegistry.register("set", new SetCommandHandler());
        CommandRegistry.register("get", new GetCommandHandler());
        CommandRegistry.register("mget", new MGetCommandHandler());
        CommandRegistry.register("mset", new MSetCommandHandler());
    }
}
