package zk_lock;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by weichen on 17/4/12.
 *
 * 连接 zk
 */

public class AbstractZookeeper implements Watcher{

    protected ZooKeeper zooKeeper;
    protected CountDownLatch countDownLatch = new CountDownLatch(1);

    /**
     * 连接zk；返回 zk 对象
     * @param hosts 连接字符串
     * @param sessionTimeOut 连接超时时间
     * @return zk连接成功对象
     * @throws IOException
     * @throws InterruptedException
     */
    public ZooKeeper connect (String hosts, int sessionTimeOut) throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(hosts, sessionTimeOut, this);
        countDownLatch.await();

        System.out.println("AbstractZookeeper.connect .. ");

        return zooKeeper;
    }

    /**
     * 重写 zk watcher 的process；watcher zk连接成功状态
     * @param watchedEvent
     */
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            countDownLatch.countDown();
        }
    }

    /**
     * 关闭zk 连接
     * @throws InterruptedException
     */
    public void close() throws InterruptedException{
        zooKeeper.close();
    }

}
