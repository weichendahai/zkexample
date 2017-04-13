package zk_lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Created by weichen on 17/4/12.
 * 分布式锁 类文件
 */
public class DistributedLock {

    private static final Logger LOG = LoggerFactory.getLogger(DistributedLock.class);

    private ZooKeeper zk  = null;
    private String selfPath;
    private String waitPath;
    private String LOG_PREFIX_OF_THREAD = Thread.currentThread().getName();

    private static final String GROUP_PATH = "/disLocks";
    private static final String SUB_PATH = "/disLocks/sub";

    private Watcher watcher;

    public DistributedLock(ZooKeeper zk) {
        this.zk = zk;
    }

    public Watcher getWatcher() {
        return watcher;
    }

    public void setWatcher(Watcher watcher) {
        this.watcher = watcher;
    }

    /**
     * 创建一个节点；然后检查，自己创建的节点，是不是最小的
     * 如果是最小的，说明自己获得 "分布式锁"
     * 不然，就 watcher 比自己小的，前一个节点；如果前一个节点"消失"，再次尝试去获取分布式锁
     *
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public boolean getLock () throws KeeperException, InterruptedException {
        /* zk服务器，创建 临时有序节点 */
        selfPath = zk.create(SUB_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        LOG.info(LOG_PREFIX_OF_THREAD + " 创建锁路径:" + selfPath);

        if (checkMinPath()) {
            return true;
        }

        return  false;
    }

    /**
     * 释放"分布式锁"
     */
    public void unlock() {
        try {
            if (zk.exists(this.selfPath, false) == null) {
                LOG.info(LOG_PREFIX_OF_THREAD + " 本节点已不在了  ... ");
            }
            zk.delete(this.selfPath, -1);
            LOG.info(LOG_PREFIX_OF_THREAD + " 删除本节点: " + selfPath);
            zk.close();

        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检测当前线程保存如zk内数据；是否是最小数据，如果是最小，便获得锁
     * 不然，找到自己前面一个锁，监控"前面一个锁"进行排队
     *
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public  boolean checkMinPath() throws KeeperException, InterruptedException {

        /* 1. 获取zk中；保存分布式锁节点（znode）下，所有锁 */
        List<String> subNodes = zk.getChildren(GROUP_PATH, false);
        /* 2. 所有锁，进行排序 */
        Collections.sort(subNodes);

        /* 3. 在锁列表（list）中，查找出当前线程，刚刚保存的锁（即写入zk服务器的节点，selfPath),是否索引为0 最小 */
        int index = subNodes.indexOf(selfPath.substring(GROUP_PATH.length() + 1));

        switch (index) {
            case -1: {
                /* 在zk服务中，没有找到 刚刚写入的 节点数据  */
                LOG.error(LOG_PREFIX_OF_THREAD + " 本节点不在了..." + selfPath);
                return false;
            }
            case 0: {
                /* 线程获取分布式锁成功 */
                LOG.info(LOG_PREFIX_OF_THREAD + " 子节点中，果然我最大" + selfPath);
                return true;
            }
            default: {

                /* 节点存在，但不是最小， 查找节点啊前一个顺序节点， 进行监听 */
                this.waitPath = GROUP_PATH+"/"+ subNodes.get(index -1);
                LOG.info(LOG_PREFIX_OF_THREAD + " 获取子节点中: "+ selfPath +" ，排在我前面的" + waitPath);

                try {
                    zk.getData(waitPath, this.watcher, new Stat());
                    return false;
                } catch (KeeperException e) {

                    if (zk.exists(waitPath, false) == null) {
                        LOG.info(LOG_PREFIX_OF_THREAD+" 子节点中: "+ selfPath +" ，排在我前面的"+waitPath+"已失踪，幸福来得太突然?");
                        return checkMinPath();
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * 创建的是固定节点；并不是临时节点
     * @param path zk 路径
     * @param data zk 路径下value
     * @return
     */
    public boolean createPath (String path, String data) {
        try {
            if (zk.exists(path, false) == null) {
                String result = zk.create(path,data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                LOG.info(LOG_PREFIX_OF_THREAD + " 节点创建成功， Path:" + result + ", content" + data);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return  true;
    }

    public String getWaitPath() {
        return waitPath;
    }

}
