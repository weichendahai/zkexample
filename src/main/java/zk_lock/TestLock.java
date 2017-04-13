package zk_lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Created by weichen on 17/4/13.
 *
 * 此类；只是个测试 分布式锁 一个例子调用；
 * 模拟10个线程，同时请求同一个server
 * server根据分布式锁争抢情况，按着在zk 中创建临时节点从小及大，逐个执行；
 */
public class TestLock {

    private static final Logger LOG = LoggerFactory.getLogger(TestLock.class);
    /* 确保所有线程运行结束 */
    private static final String CONNECTION_STRING = "192.168.80.201:2181";
    private static final int THREAD_NUM = 10;
    public static CountDownLatch threadSemaphore = new CountDownLatch(THREAD_NUM);
    private static final String GROUP_PATH = "/disLocks";
    private static final String SUB_PATH = "/disLocks/sub";
    private static final int SESSION_TIMEOUT = 10000;

    public static void main(String[] args) {

        /*模拟10个线程，争抢分布式锁，同时一起请求同一个服务*/
        for (int i = 0; i < THREAD_NUM; i++) {
            final int threadId = i;

            /* 模拟每个线程中，要做的事情 */
            final DoTemplate doTemplateTest = new DoTemplate() {
                public void dodo() {
                    try {
                        Thread.sleep(1000);
                        LOG.info(Thread.currentThread().getName() + " 我要修改一个文件 ... " + threadId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

           new Thread(new Runnable(){
               public void run() {
                   try {
                       new LockService().doService(doTemplateTest);
                   } catch (Exception e) {
                       LOG.error("【第"+threadId+"个线程】 抛出的异常：");
                       e.printStackTrace();
                   }
               }

           }).start();
        }

        try {
            /* 检测到10个线程都完成;延迟1秒让线程log输出完成 */
            threadSemaphore.await();
            Thread.sleep(1000);
            LOG.info("所有线程运行结束");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/*
log 信息

多线程，执行顺序，按线程在zk服务器创建锁（临时有序节点），从小到大逐个执行

AbstractZookeeper.connect ..
AbstractZookeeper.connect ..
AbstractZookeeper.connect ..
AbstractZookeeper.connect ..
2017-04-13 16:33:41 INFO  DistributedLock:53 - Thread-1 创建锁路径:/disLocks/sub0000000172
2017-04-13 16:33:41 INFO  DistributedLock:53 - Thread-8 创建锁路径:/disLocks/sub0000000173
2017-04-13 16:33:41 INFO  DistributedLock:53 - Thread-9 创建锁路径:/disLocks/sub0000000171
2017-04-13 16:33:41 INFO  DistributedLock:53 - Thread-0 创建锁路径:/disLocks/sub0000000174
2017-04-13 16:33:41 INFO  DistributedLock:53 - Thread-2 创建锁路径:/disLocks/sub0000000175
2017-04-13 16:33:41 INFO  DistributedLock:114 - Thread-1 获取子节点中: /disLocks/sub0000000172 ，排在我前面的/disLocks/sub0000000171
2017-04-13 16:33:41 INFO  DistributedLock:107 - Thread-9 子节点中，果然我最大/disLocks/sub0000000171
2017-04-13 16:33:41 INFO  DistributedLock:114 - Thread-8 获取子节点中: /disLocks/sub0000000173 ，排在我前面的/disLocks/sub0000000172
2017-04-13 16:33:41 INFO  DistributedLock:114 - Thread-2 获取子节点中: /disLocks/sub0000000175 ，排在我前面的/disLocks/sub0000000174
2017-04-13 16:33:41 INFO  DistributedLock:114 - Thread-0 获取子节点中: /disLocks/sub0000000174 ，排在我前面的/disLocks/sub0000000173
2017-04-13 16:33:41 INFO  LockWatcher:57 - Thread-9 获取锁锁成功， 赶紧干活
2017-04-13 16:33:41 INFO  ClientCnxn:975 - Opening socket connection to server 192.168.178.151/192.168.178.151:2181. Will not attempt to authenticate using SASL (unknown error)
2017-04-13 16:33:41 INFO  ClientCnxn:852 - Socket connection established to 192.168.178.151/192.168.178.151:2181, initiating session
2017-04-13 16:33:41 INFO  ClientCnxn:1235 - Session establishment complete on server 192.168.178.151/192.168.178.151:2181, sessionid = 0x15b5fcac60a0069, negotiated timeout = 10000
AbstractZookeeper.connect ..
2017-04-13 16:33:41 INFO  DistributedLock:53 - Thread-6 创建锁路径:/disLocks/sub0000000176
2017-04-13 16:33:41 INFO  DistributedLock:114 - Thread-6 获取子节点中: /disLocks/sub0000000176 ，排在我前面的/disLocks/sub0000000175
2017-04-13 16:33:42 INFO  ClientCnxn:975 - Opening socket connection to server 192.168.178.152/192.168.178.152:2181. Will not attempt to authenticate using SASL (unknown error)
2017-04-13 16:33:42 INFO  ClientCnxn:852 - Socket connection established to 192.168.178.152/192.168.178.152:2181, initiating session
2017-04-13 16:33:42 INFO  ClientCnxn:1235 - Session establishment complete on server 192.168.178.152/192.168.178.152:2181, sessionid = 0x25b5f720707006c, negotiated timeout = 10000
AbstractZookeeper.connect ..
2017-04-13 16:33:42 INFO  DistributedLock:53 - Thread-4 创建锁路径:/disLocks/sub0000000177
2017-04-13 16:33:42 INFO  DistributedLock:114 - Thread-4 获取子节点中: /disLocks/sub0000000177 ，排在我前面的/disLocks/sub0000000176
2017-04-13 16:33:42 INFO  ClientCnxn:975 - Opening socket connection to server 192.168.178.151/192.168.178.151:2181. Will not attempt to authenticate using SASL (unknown error)
2017-04-13 16:33:42 INFO  ClientCnxn:852 - Socket connection established to 192.168.178.151/192.168.178.151:2181, initiating session
2017-04-13 16:33:42 INFO  ClientCnxn:1235 - Session establishment complete on server 192.168.178.151/192.168.178.151:2181, sessionid = 0x15b5fcac60a006a, negotiated timeout = 10000
AbstractZookeeper.connect ..
2017-04-13 16:33:42 INFO  DistributedLock:53 - Thread-7 创建锁路径:/disLocks/sub0000000178
2017-04-13 16:33:42 INFO  DistributedLock:114 - Thread-7 获取子节点中: /disLocks/sub0000000178 ，排在我前面的/disLocks/sub0000000177
2017-04-13 16:33:42 INFO  ClientCnxn:975 - Opening socket connection to server 192.168.178.151/192.168.178.151:2181. Will not attempt to authenticate using SASL (unknown error)
2017-04-13 16:33:42 INFO  ClientCnxn:852 - Socket connection established to 192.168.178.151/192.168.178.151:2181, initiating session
2017-04-13 16:33:42 INFO  ClientCnxn:1235 - Session establishment complete on server 192.168.178.151/192.168.178.151:2181, sessionid = 0x15b5fcac60a006b, negotiated timeout = 10000
AbstractZookeeper.connect ..
2017-04-13 16:33:42 INFO  DistributedLock:53 - Thread-3 创建锁路径:/disLocks/sub0000000179
2017-04-13 16:33:42 INFO  DistributedLock:114 - Thread-3 获取子节点中: /disLocks/sub0000000179 ，排在我前面的/disLocks/sub0000000178
2017-04-13 16:33:42 INFO  ClientCnxn:975 - Opening socket connection to server 192.168.178.152/192.168.178.152:2181. Will not attempt to authenticate using SASL (unknown error)
2017-04-13 16:33:42 INFO  ClientCnxn:852 - Socket connection established to 192.168.178.152/192.168.178.152:2181, initiating session
2017-04-13 16:33:42 INFO  ClientCnxn:1235 - Session establishment complete on server 192.168.178.152/192.168.178.152:2181, sessionid = 0x25b5f720707006d, negotiated timeout = 10000
AbstractZookeeper.connect ..
2017-04-13 16:33:42 INFO  DistributedLock:53 - Thread-5 创建锁路径:/disLocks/sub0000000180
2017-04-13 16:33:42 INFO  DistributedLock:114 - Thread-5 获取子节点中: /disLocks/sub0000000180 ，排在我前面的/disLocks/sub0000000179
2017-04-13 16:33:42 INFO  TestLock:37 - Thread-9 我要修改一个文件 ... 9
2017-04-13 16:33:42 INFO  DistributedLock:71 - Thread-9 删除本节点: /disLocks/sub0000000171
2017-04-13 16:33:42 INFO  DistributedLock:107 - Thread-1 子节点中，果然我最大/disLocks/sub0000000172
2017-04-13 16:33:42 INFO  LockWatcher:57 - Thread-1-EventThread 获取锁锁成功， 赶紧干活
2017-04-13 16:33:42 INFO  ZooKeeper:684 - Session: 0x25b5f720707006a closed
2017-04-13 16:33:42 INFO  ClientCnxn:512 - EventThread shut down
2017-04-13 16:33:43 INFO  TestLock:37 - Thread-1-EventThread 我要修改一个文件 ... 1
2017-04-13 16:33:43 INFO  DistributedLock:71 - Thread-1 删除本节点: /disLocks/sub0000000172
2017-04-13 16:33:43 INFO  DistributedLock:107 - Thread-8 子节点中，果然我最大/disLocks/sub0000000173
2017-04-13 16:33:43 INFO  LockWatcher:57 - Thread-8-EventThread 获取锁锁成功， 赶紧干活
2017-04-13 16:33:43 INFO  ZooKeeper:684 - Session: 0x15b5fcac60a0068 closed
2017-04-13 16:33:43 INFO  ClientCnxn:512 - EventThread shut down
2017-04-13 16:33:44 INFO  TestLock:37 - Thread-8-EventThread 我要修改一个文件 ... 8
2017-04-13 16:33:44 INFO  DistributedLock:71 - Thread-8 删除本节点: /disLocks/sub0000000173
2017-04-13 16:33:44 INFO  DistributedLock:107 - Thread-0 子节点中，果然我最大/disLocks/sub0000000174
2017-04-13 16:33:44 INFO  LockWatcher:57 - Thread-0-EventThread 获取锁锁成功， 赶紧干活
2017-04-13 16:33:44 INFO  ZooKeeper:684 - Session: 0x15b5fcac60a0067 closed
2017-04-13 16:33:44 INFO  ClientCnxn:512 - EventThread shut down
2017-04-13 16:33:45 INFO  TestLock:37 - Thread-0-EventThread 我要修改一个文件 ... 0
2017-04-13 16:33:45 INFO  DistributedLock:71 - Thread-0 删除本节点: /disLocks/sub0000000174
2017-04-13 16:33:45 INFO  DistributedLock:107 - Thread-2 子节点中，果然我最大/disLocks/sub0000000175
2017-04-13 16:33:45 INFO  LockWatcher:57 - Thread-2-EventThread 获取锁锁成功， 赶紧干活
2017-04-13 16:33:45 INFO  ZooKeeper:684 - Session: 0x25b5f7207070069 closed
2017-04-13 16:33:45 INFO  ClientCnxn:512 - EventThread shut down
2017-04-13 16:33:46 INFO  TestLock:37 - Thread-2-EventThread 我要修改一个文件 ... 2
2017-04-13 16:33:46 INFO  DistributedLock:71 - Thread-2 删除本节点: /disLocks/sub0000000175
2017-04-13 16:33:46 INFO  DistributedLock:107 - Thread-6 子节点中，果然我最大/disLocks/sub0000000176
2017-04-13 16:33:46 INFO  LockWatcher:57 - Thread-6-EventThread 获取锁锁成功， 赶紧干活
2017-04-13 16:33:46 INFO  ZooKeeper:684 - Session: 0x25b5f720707006b closed
2017-04-13 16:33:46 INFO  ClientCnxn:512 - EventThread shut down
2017-04-13 16:33:47 INFO  TestLock:37 - Thread-6-EventThread 我要修改一个文件 ... 6
2017-04-13 16:33:47 INFO  DistributedLock:71 - Thread-6 删除本节点: /disLocks/sub0000000176
2017-04-13 16:33:47 INFO  DistributedLock:107 - Thread-4 子节点中，果然我最大/disLocks/sub0000000177
2017-04-13 16:33:47 INFO  LockWatcher:57 - Thread-4-EventThread 获取锁锁成功， 赶紧干活
2017-04-13 16:33:47 INFO  ZooKeeper:684 - Session: 0x15b5fcac60a0069 closed
2017-04-13 16:33:47 INFO  ClientCnxn:512 - EventThread shut down
2017-04-13 16:33:48 INFO  TestLock:37 - Thread-4-EventThread 我要修改一个文件 ... 4
2017-04-13 16:33:48 INFO  DistributedLock:71 - Thread-4 删除本节点: /disLocks/sub0000000177
2017-04-13 16:33:48 INFO  DistributedLock:107 - Thread-7 子节点中，果然我最大/disLocks/sub0000000178
2017-04-13 16:33:48 INFO  LockWatcher:57 - Thread-7-EventThread 获取锁锁成功， 赶紧干活
2017-04-13 16:33:48 INFO  ZooKeeper:684 - Session: 0x25b5f720707006c closed
2017-04-13 16:33:48 INFO  ClientCnxn:512 - EventThread shut down
2017-04-13 16:33:49 INFO  TestLock:37 - Thread-7-EventThread 我要修改一个文件 ... 7
2017-04-13 16:33:49 INFO  DistributedLock:71 - Thread-7 删除本节点: /disLocks/sub0000000178
2017-04-13 16:33:49 INFO  DistributedLock:107 - Thread-3 子节点中，果然我最大/disLocks/sub0000000179
2017-04-13 16:33:49 INFO  LockWatcher:57 - Thread-3-EventThread 获取锁锁成功， 赶紧干活
2017-04-13 16:33:49 INFO  ZooKeeper:684 - Session: 0x15b5fcac60a006a closed
2017-04-13 16:33:49 INFO  ClientCnxn:512 - EventThread shut down
2017-04-13 16:33:50 INFO  TestLock:37 - Thread-3-EventThread 我要修改一个文件 ... 3
2017-04-13 16:33:50 INFO  DistributedLock:71 - Thread-3 删除本节点: /disLocks/sub0000000179
2017-04-13 16:33:50 INFO  DistributedLock:107 - Thread-5 子节点中，果然我最大/disLocks/sub0000000180
2017-04-13 16:33:50 INFO  LockWatcher:57 - Thread-5-EventThread 获取锁锁成功， 赶紧干活
2017-04-13 16:33:50 INFO  ZooKeeper:684 - Session: 0x15b5fcac60a006b closed
2017-04-13 16:33:50 INFO  ClientCnxn:512 - EventThread shut down
2017-04-13 16:33:51 INFO  TestLock:37 - Thread-5-EventThread 我要修改一个文件 ... 5
2017-04-13 16:33:51 INFO  DistributedLock:71 - Thread-5 删除本节点: /disLocks/sub0000000180
2017-04-13 16:33:51 INFO  ZooKeeper:684 - Session: 0x25b5f720707006d closed
2017-04-13 16:33:51 INFO  ClientCnxn:512 - EventThread shut down
2017-04-13 16:33:52 INFO  TestLock:61 - 所有线程运行结束

Process finished with exit code 0

 */
