package rmi_demo.rmi;

import rmi_demo.common.HelloService;

import java.rmi.Naming;

/**
 * Created by weichen on 17/4/12.
 */
public class RmiClientDemo {

    public static void main(String[] args) throws Exception {
//        String url = "rmi_demo.rmi://localhost:1987/demo.zookeeper.remoting.rmi_demo.server.HelloServiceImpl";
        String url = "rmi://localhost:1987/test/rmi_demo.rmi";
        HelloService helloService = (HelloService) Naming.lookup(url);
        String result = helloService.sayHello("Jack23");
        System.out.println(result);
    }
}
