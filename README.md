# Cache-Forge

#### 项目树状结构
```plantuml
CacheForge
├── .idea
└── src
    └── main
        └── java
            └── fun.redis.cacheforge
                ├── config          # 配置模块：管理项目配置、参数加载
                │   ├── Config.java
                │   ├── ConfigLoader.java
                │   └── ConfigConstants.java
                ├── storage         # 存储模块：核心数据结构（如哈希表、跳表等）
                │   ├── data
                │   │   ├── CacheDataStructure.java
                │   │   ├── HashTable.java
                │   │   ├── SkipList.java
                │   │   └── ...
                │   ├── repository
                │   │   ├── KeyValueRepository.java
                │   │   └── RepositoryImpl.java
                │   └── entity
                │       ├── CacheEntry.java
                │       └── ExpiredEntry.java
                ├── persistence     # 持久化模块：RDB、AOF 持久化实现
                │   ├── RdbPersistence.java
                │   ├── AofPersistence.java
                │   └── PersistenceManager.java
                ├── replication     # 主从复制模块：主节点、从节点逻辑
                │   ├── master
                │   │   ├── MasterReplication.java
                │   │   └── ReplicationTask.java
                │   ├── slave
                │   │   ├── SlaveReplication.java
                │   │   └── SyncManager.java
                │   └── ReplicationProtocol.java
                ├── command         # 命令模块：工厂+策略模式实现命令解析、执行
                │   ├── factory
                │   │   ├── CommandFactory.java
                │   │   └── CommandFactoryImpl.java
                │   ├── strategy
                │   │   ├── CommandStrategy.java
                │   │   ├── GetCommand.java
                │   │   ├── SetCommand.java
                │   │   └── ...
                │   └── parser
                │       ├── CommandParser.java
                │       └── RedisProtocolParser.java
                ├── protocol        # 原协议包：可扩展为通信协议封装
                │   ├── RedisProtocol.java
                │   └── ConnectionHandler.java
                └── server          # 原服务包：服务启动、客户端连接管理
                    ├── ServerBootstrap.java
                    ├── ClientHandler.java
                    └── ServerConfig.java