package zk_lock;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by weichen on 17/4/13.
 */
public class LockService {

    private static final Logger LOG = LoggerFactory.getLogger(LockService.class);

    private static final String CONNECTION_STRING="192.168.178.151:2181,192.168.178.152:2181,192.168.178.153:2181";
    private static final int THREAD_NUM = 10;
    public static CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM);

    private static final String GROUP_PATH = "/disLocks";
    private static final String SUB_PATH = "/dislocks/sub";
    private static final int SESSION_TIMEOUT = 10000;
    AbstractZookeeper abstractZookeeper = new AbstractZookeeper();

    /**
     * 被客户端调用的，需要争抢"分布式所"的方法
     * @param doTemplate
     */
    public void doService (DoTemplate doTemplate) {
        ZooKeeper zk = null;
        try {

            /* 1. 创建一个zk 对象 */
            zk = abstractZookeeper.connect(CONNECTION_STRING, SESSION_TIMEOUT);
            /* 2. 创建一个分布式锁对象 */
            DistributedLock distributedLock = new DistributedLock(zk);
            /* 3. 根据 分布式锁对象 和 传入的具体的处理逻辑代码，创建一个锁监控 lockWatcher */
            LockWatcher lockWatcher = new LockWatcher(distributedLock, doTemplate);
            /* 4. 给 分布式锁（distributedLock）设置监听（lockWatcher） */
            distributedLock.setWatcher(lockWatcher);
            /* 5. 在zk服务器，创建 分布式锁 的跟目录 */
            distributedLock.createPath(GROUP_PATH, "节点由线程" + Thread.currentThread().getName() + "创建");
            /* 6. 尝试获取 分布式锁，如果成功执行具体逻辑 */
            boolean rs = distributedLock.getLock();

            if (rs == true) {
                lockWatcher.doSomeThing();
                distributedLock.unlock();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();

        }
    }
}
