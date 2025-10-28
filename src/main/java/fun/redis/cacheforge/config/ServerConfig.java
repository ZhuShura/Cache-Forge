package fun.redis.cacheforge.config;

import fun.redis.cacheforge.command.CommandRegistry;
import fun.redis.cacheforge.command.handler.impl.CommandCommandHandler;
import fun.redis.cacheforge.command.handler.impl.list.*;
import fun.redis.cacheforge.command.handler.impl.string.GetCommandHandler;
import fun.redis.cacheforge.command.handler.impl.string.SetCommandHandler;

public class ServerConfig {
    public final int PORT = 6379;

    public ServerConfig() {
        // ---------------------Normal-------------------
        CommandRegistry.register("command", new CommandCommandHandler());
//        CommandRegistry.register("del", new DelCommandHandler());
        // ---------------------String-------------------
        CommandRegistry.register("set", new SetCommandHandler());
        CommandRegistry.register("get", new GetCommandHandler());
//        CommandRegistry.register("mget", new MGetCommandHandler());
//        CommandRegistry.register("mset", new MSetCommandHandler());
//        CommandRegistry.register("incr", new IncrCommandHandler());
//        CommandRegistry.register("decr", new DecrCommandHandler());
//        CommandRegistry.register("incrby", new IncrByCommandHandler());
//        CommandRegistry.register("decrby", new DecrByCommandHandler());
//        CommandRegistry.register("append", new AppendCommandHandler());
//        CommandRegistry.register("strlen", new StrlenCommandHandler());
        // ---------------------List-------------------
        CommandRegistry.register("lpush", new LPushCommandHandler());
        CommandRegistry.register("rpush", new RPushCommandHandler());
        CommandRegistry.register("lpop", new LPopCommandHandler());
        CommandRegistry.register("rpop", new RPopCommandHandler());
        CommandRegistry.register("lrange", new LRangeCommandHandler());
        CommandRegistry.register("lindex", new LIndexCommandHandler());
        CommandRegistry.register("llen", new LLenCommandHandler());
        CommandRegistry.register("lset", new LSetCommandHandler());
        CommandRegistry.register("linsert", new LInsertCommandHandler());

        CommandRegistry.register("ltrim", new LTrimCommandHandler());
        CommandRegistry.register("lrem", new LRemCommandHandler());
        CommandRegistry.register("lmove", new LMoveCommandHandler());
    }
}