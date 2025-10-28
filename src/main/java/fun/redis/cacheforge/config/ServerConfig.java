package fun.redis.cacheforge.config;

import fun.redis.cacheforge.command.CommandRegistry;
import fun.redis.cacheforge.command.handler.impl.CommandCommandHandler;
import fun.redis.cacheforge.command.handler.impl.string.*;

public class ServerConfig {
    public final int PORT = 6379;

    public ServerConfig() {
        // ---------------------Normal-------------------
        CommandRegistry.register("command", new CommandCommandHandler());
        // ---------------------String-------------------
        CommandRegistry.register("append", new AppendCommandHandler());
        CommandRegistry.register("decrby", new DecrByCommandHandler());
        CommandRegistry.register("decr", new DecrCommandHandler());
        CommandRegistry.register("get", new GetCommandHandler());
        CommandRegistry.register("getDel", new GetDelCommandHandler());
        CommandRegistry.register("getex", new GetExCommandHandler());
        CommandRegistry.register("getrange", new GetRangeCommandHandler());
        CommandRegistry.register("incrby", new IncrByCommandHandler());
        CommandRegistry.register("incr", new IncrCommandHandler());
        CommandRegistry.register("lcs", new LCSCommandHandler());
        CommandRegistry.register("psetex", new PSetExCommandHandler());
        CommandRegistry.register("set", new SetCommandHandler());
        CommandRegistry.register("setex", new SetExCommandHandler());
        CommandRegistry.register("setnx", new SetNxCommandHandler());
        CommandRegistry.register("setrange", new SetRangeCommandHandler());
        CommandRegistry.register("strlen", new StrLenCommandHandler());
    }
}