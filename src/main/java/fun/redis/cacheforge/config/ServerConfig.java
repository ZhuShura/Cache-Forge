package fun.redis.cacheforge.config;

import fun.redis.cacheforge.command.CommandRegistry;
import fun.redis.cacheforge.command.handler.impl.CommandCommandHandler;
import fun.redis.cacheforge.command.handler.impl.string.*;
import fun.redis.cacheforge.command.handler.impl.list.*;
import fun.redis.cacheforge.command.handler.impl.set.SAddCommandHandler;
import fun.redis.cacheforge.command.handler.impl.set.SCardCommandHandler;
import fun.redis.cacheforge.command.handler.impl.set.SDiffCommandHandler;
import fun.redis.cacheforge.command.handler.impl.set.SDiffStoreCommandHandler;
import fun.redis.cacheforge.command.handler.impl.string.GetCommandHandler;
import fun.redis.cacheforge.command.handler.impl.string.SetCommandHandler;

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
        CommandRegistry.register("incrbyfloat", new IncrByFloatCommandHandler());
        CommandRegistry.register("incr", new IncrCommandHandler());
        CommandRegistry.register("lcs", new LCSCommandHandler());
        CommandRegistry.register("mget", new MGetCommandHandler());
        CommandRegistry.register("mset", new MSetCommandHandler());
        CommandRegistry.register("msetnx", new MSetNxCommandHandler());
        CommandRegistry.register("psetex", new PSetExCommandHandler());
        CommandRegistry.register("set", new SetCommandHandler());
        CommandRegistry.register("setex", new SetExCommandHandler());
        CommandRegistry.register("setnx", new SetNxCommandHandler());
        CommandRegistry.register("setrange", new SetRangeCommandHandler());
        CommandRegistry.register("strlen", new StrLenCommandHandler());
    }
}