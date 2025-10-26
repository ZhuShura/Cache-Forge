package fun.redis.cacheforge.config;

import fun.redis.cacheforge.command.CommandRegistry;
import fun.redis.cacheforge.command.handler.impl.SetCommandHandler;

public class ServerConfig {
    public final int PORT = 6379;

    public ServerConfig() {
        CommandRegistry.register("set", new SetCommandHandler());
    }
}
