package rmi_demo.common;

/**
 * Created by weichen on 17/4/12.
 */
public interface Constant {

    String ZK_CONNECTION_STRING = "192.168.178.151:2181,192.168.178.152:2181,192.168.178.153:2181";
    int ZK_SESSION_TIMEOUT = 5000;
    String ZK_REGISTRY_PATH = "/registry";
    String ZK_PROVIDER_PATH = ZK_REGISTRY_PATH + "/provider";
}
