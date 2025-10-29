package fun.redis.cacheforge.config;

import fun.redis.cacheforge.command.CommandRegistry;
import fun.redis.cacheforge.command.handler.impl.CommandCommandHandler;
import fun.redis.cacheforge.command.handler.impl.set.*;
import fun.redis.cacheforge.command.handler.impl.string.*;
import fun.redis.cacheforge.command.handler.impl.list.*;

public class ServerConfig {
    public final int PORT = 6379;

    public ServerConfig() {
        // --------------------Normal-------------------
        CommandRegistry.register("command", new CommandCommandHandler());
        // done----------------String-------------------
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
        // done-----------------List-------------------
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
        CommandRegistry.register("rpoplpush", new RPopLPushCommandHandler());
        CommandRegistry.register("rpush", new RPushCommandHandler());
        // ---------------------Set-------------------
        CommandRegistry.register("sadd", new SAddCommandHandler());
        CommandRegistry.register("scard", new SCardCommandHandler());
        CommandRegistry.register("sdiff", new SDiffCommandHandler());
        CommandRegistry.register("sdiffstore", new SDiffStoreCommandHandler());
        CommandRegistry.register("sintercard", new SInterCardCommandHandler());
        CommandRegistry.register("sinter", new SInterCommandHandler());
        CommandRegistry.register("sinterstore", new SInterStoreCommandHandler());
        CommandRegistry.register("sismember", new SIsMemberCommandHandler());
        CommandRegistry.register("smembers", new SMembersCommandHandler());
        CommandRegistry.register("smismember", new SMIsMemberCommandHandler());
        CommandRegistry.register("smove", new SMoveCommandHandler());
        CommandRegistry.register("spop", new SPopCommandHandler());
    }
}