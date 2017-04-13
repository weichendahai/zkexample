package rmi_demo.server;

import rmi_demo.common.Constant;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.CountDownLatch;

/**
 * Created by weichen on 17/4/12.
 */

/*注册服务防范*/
public class ServiceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceProvider.class);

    /*用于等待 异步连接（syncConnected）事件触发后，继续执行线程*/
    private CountDownLatch latch = new CountDownLatch(1);

    public void publish (Remote remote, String host, int port) {

        /* 1.发布 RMI 服务；并返回RMI 地址 */
        String url = publishService(remote, host, port);

        if (url != null) {
            /* 2.连接 zk 服务器， 获取zk 连接对象 */
            ZooKeeper zk = connectZkServer();

            if (zk != null) {
                /* 3.创建 zk node， 并将 RMI 地址，保存如 zk node 上 */
                createZkNode(zk, url);
            }
        }

    }

    /**
     * 发布 RMI 服务到本地仓库中
     */
    private String publishService (Remote remote, String host, int port) {
        String url = null;
        try {
            url = String.format("rmi://%s:%d/%s", host, port, remote.getClass().getName());
            LocateRegistry.createRegistry(port);
            Naming.rebind(url, remote);

        } catch (RemoteException  e) {
            e.printStackTrace();
        } catch (  MalformedURLException e) {
            e.printStackTrace();
        }
        return  url;
    }

    /**
     * 链接zk 服务器
     * @return
     */
    private ZooKeeper connectZkServer () {
        ZooKeeper  zk = null;

        try {
            zk = new ZooKeeper(Constant.ZK_CONNECTION_STRING, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    /*zk 连接成功；然后通知（唤醒） 主线程 继续执行*/
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });

            /*当前线程，处于阻塞状态，等待链接线程完成，继续执行*/
            latch.await();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return  zk;
    }

    /**
     * 在zk服务器，创建 一个临时序列节点
     * @param zk
     * @param data
     */
    private void createZkNode (ZooKeeper zk, String data) {
        try {
            byte[] nodeData = data.getBytes();
            /* 创建一个具有临时序列的 zk node */
            String path = zk.create(Constant.ZK_PROVIDER_PATH, nodeData, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.debug("create zookeeper node ({} => {})", path, data);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
