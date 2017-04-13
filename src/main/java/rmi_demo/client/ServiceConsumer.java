package rmi_demo.client;

import rmi_demo.common.Constant;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by weichen on 17/4/12.
 */
public class ServiceConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceConsumer.class);

    /* 用于等待 SyncConnected 事件触发后继续执行当前线程 */
    private CountDownLatch latch = new CountDownLatch(1);

    /*
    定义一个 volatile 成员变量， 用于保存最新 RMI 地址
    （考虑到该变量或许被其他线程所修改，一旦修改后，该变量的值会影响到所有线程，volatile 跨线程可见）
    */
    private volatile List<String> urlList = new ArrayList<String>();

    public ServiceConsumer() {
        ZooKeeper zk = connectZkServer();

        if (zk != null) {
            watchZkNode(zk);
        }
    }

    /**
     *   客户端，链接 zk rmi_demo.server
     * @return
     */
    private ZooKeeper connectZkServer () {
        ZooKeeper zk = null;

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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return zk;
    }

    private void watchZkNode (final ZooKeeper zk)  {

        try {
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                        /*如果子节点变化，则重新调用该方法（为获取最新子节点中的数据）*/
                        watchZkNode(zk);
                    }
                }
            });

            /* 用于存贮； /registry 所有子节点中的数据 */
            List<String> dataList = new ArrayList<String>();
             /* 获取 /registry 的子节点中的数据 */
            for (String node : nodeList) {
                byte[] data = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(data));
            }
            LOGGER.debug("node data: {}", dataList);
            urlList = dataList; // 更新最新的 RMI 地址

        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 查找 RMI 服务
     * @param <T>
     * @return
     */
    public <T extends Remote> T lookup () {
        T service = null;

        int size = urlList.size();

        if ( size > 0 ) {
            String url ;
            if (size == 1) {
                url = urlList.get(0);
                LOGGER.debug("using only url: {}", url);
            } else {
                /* 若 urlList 中存在多个元素，则随机获取一个元素 */
                url = urlList.get(ThreadLocalRandom.current().nextInt(size));
            }

            /* 根据url 地址； 从 JNDI 中查找 RMI 服务 */
            service = lookupService(url);
        }

        return service;
    }

    /**
     * 在JNDI 中查找 RMI 远程访问对象
     * @param url
     * @param <T>
     * @return
     */
    private <T> T lookupService (String url) {
        T remote = null;
        try {
            /* RMI 根据url，查找响应"对象" */
            remote = (T) Naming.lookup(url);
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return  remote;
    }

}
