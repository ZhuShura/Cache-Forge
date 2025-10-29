package fun.redis.cacheforge.config;

import fun.redis.cacheforge.command.CommandRegistry;
import fun.redis.cacheforge.command.handler.impl.CommandCommandHandler;
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
//        CommandRegistry.register("del", new DelCommandHandler());
        // ---------------------String-------------------
        CommandRegistry.register("set", new SetCommandHandler());
        CommandRegistry.register("get", new GetCommandHandler());
        // ---------------------List-------------------
        CommandRegistry.register("lindex", new LIndexCommandHandler());
        CommandRegistry.register("linsert", new LInsertCommandHandler());
        CommandRegistry.register("llen", new LLenCommandHandler());
        CommandRegistry.register("lmove", new LMoveCommandHandler());
        CommandRegistry.register("lpop", new LPopCommandHandler());
        CommandRegistry.register("lpos", new LPosCommandHandler());
        CommandRegistry.register("lpush", new LPushCommandHandler());
        CommandRegistry.register("lrange", new LRangeCommandHandler());
        CommandRegistry.register("lrem", new LRemCommandHandler());
        CommandRegistry.register("lset", new LSetCommandHandler());
        CommandRegistry.register("ltrim", new LTrimCommandHandler());
        CommandRegistry.register("rpop", new RPopCommandHandler());
        CommandRegistry.register("rpush", new RPushCommandHandler());
        // ---------------------Set-------------------
        CommandRegistry.register("sadd", new SAddCommandHandler());
        CommandRegistry.register("scard", new SCardCommandHandler());
        CommandRegistry.register("sdiff", new SDiffCommandHandler());
        CommandRegistry.register("sdiffstore", new SDiffStoreCommandHandler());
    }
}