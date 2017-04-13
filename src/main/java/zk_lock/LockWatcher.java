package zk_lock;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by weichen on 17/4/13.
 * 监听锁；当锁头"消失"时，尝试获取 分布式锁， 成功获得后，执行具体逻辑
 */
public class LockWatcher implements Watcher {

    private static final Logger LOG = LoggerFactory.getLogger(LockWatcher.class);

    private DistributedLock distributedLock;
    private DoTemplate doTemplate;

    /**
     * 构造函数：构造zk 监控锁
     * @param distributedLock 分布式锁
     * @param doTemplate      进程活得锁之后，需要做的任务或是具体逻辑
     */
    public LockWatcher(DistributedLock distributedLock, DoTemplate doTemplate) {
        this.distributedLock = distributedLock;
        this.doTemplate = doTemplate;
    }

    /**
     * 如果发现zk中内节点"消失"，且消失的节点，是当前线程创建锁信息的，前一个锁；
     * 那么此线程，尝试获取 "分布式所"；如果成功，执行具体业务逻辑
     *
     * 重写（@Override）系统 Watcher 接口 的process方法
     * @param watchedEvent 监听事件
     */
    public void process(WatchedEvent watchedEvent) {
        /* watcher 到zk 节点消失，且是排队在我前面那个节点 */
        if (watchedEvent.getType() == Event.EventType.NodeDeleted && watchedEvent.getPath().equals(distributedLock.getWaitPath())) {
            try {
                if (distributedLock.checkMinPath()) {
                    doSomeThing ();
                    distributedLock.unlock();
                }
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取锁之后，具体执行的业务逻辑代码
     */
    public void doSomeThing () {
        LOG.info(Thread.currentThread().getName() + " 获取锁锁成功， 赶紧干活");
        doTemplate.dodo();
        /* 通知主线程；工作完成 */
        TestLock.threadSemaphore.countDown();
    }
}
