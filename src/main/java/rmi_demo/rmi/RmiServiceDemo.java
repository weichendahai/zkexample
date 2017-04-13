package rmi_demo.rmi;

import rmi_demo.common.HelloServiceImpl;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Created by weichen on 17/4/12.
 */
public class RmiServiceDemo  {

    public static void main(String[] args) throws Exception{
        int port = 1987;
//        String url = "rmi_demo.rmi://localhost:1987/demo.zookeeper.remoting.rmi_demo.server.HelloServiceImpl";
        String url = "rmi://localhost:1987/test/rmi_demo.rmi";
        LocateRegistry.createRegistry(port);
        Naming.rebind(url, new HelloServiceImpl());
    }
}
