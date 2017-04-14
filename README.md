# zkexample

## 项目说明

此项目是zookeeper测试项目，包含三个小例子
- 1. RMI Server 直接远程调用
- 2. RMI Server 使用 zookeeper 实现高可用方案
- 3. 基于zookeeper 的 临时有序锁机制（EPHEMERAL_SEQUENTIAL）

##  目录结构如下
```
├── README.md
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   ├── rmi_demo
│   │   │   │   ├── client
│   │   │   │   │   ├── Client.java
│   │   │   │   │   └── ServiceConsumer.java
│   │   │   │   ├── common
│   │   │   │   │   ├── Constant.java
│   │   │   │   │   ├── HelloService.java
│   │   │   │   │   └── HelloServiceImpl.java
│   │   │   │   ├── rmi
│   │   │   │   │   ├── RmiClientDemo.java
│   │   │   │   │   └── RmiServiceDemo.java
│   │   │   │   └── server
│   │   │   │       ├── Server.java
│   │   │   │       └── ServiceProvider.java
│   │   │   └── zk_lock
│   │   │       ├── AbstractZookeeper.java
│   │   │       ├── DistributedLock.java
│   │   │       ├── DoTemplate.java
│   │   │       ├── LockService.java
│   │   │       ├── LockWatcher.java
│   │   │       ├── TestLock.java
│   │   │       └── tt.java
│   │   └── resources
│   │       └── log4j.properties
│   └── test
│       └── java
└── zkexample.iml
```

## 说明

1.  RMI Server 直接远程调用
```
1.1 运行RmiServer
1.2 运行RmiClient
```

2. RMI Server 使用 zookeeper 实现高可用方案

注意: 需要先搭建zookeeper 集群

```
1.1 启动zookeeper 集群
1.2 运行 RMI Server
1.3 运行RmiClient
```

3. 基于zookeeper 的 临时有序锁机制（EPHEMERAL_SEQUENTIAL）


## 常用的四种方案:

常用技术方案:
```
- 1. 基于数据库表做乐观锁，用于分布式锁。
- 2. 使用memcached的add()方法，用于分布式锁。
- 3. 使用redis的setnx()、expire()方法，用于分布式锁。
- 4. 使用redis的setnx()、get()、getset()方法，用于分布式锁。
```
不常用但是可以用于技术方案探讨的:

```
1. 使用memcached的cas()方法，用于分布式锁。 
2. 使用redis的watch、multi、exec命令，用于分布式锁。
3. 使用zookeeper，用于分布式锁。
```