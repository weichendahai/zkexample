package rmi_demo.server;

import rmi_demo.common.HelloService;
import rmi_demo.common.HelloServiceImpl;

/**
 * Created by weichen on 17/4/12.
 */
public class Server {

    public static void main(String[] args) throws Exception {

        /* RMI 服务Ip和端口； 继承 Remote 的类，注册如这个Ip和端口 */
        String host = "localhost";
        int port = 1988;

        ServiceProvider serviceProvider = new ServiceProvider();

        HelloService helloService = new HelloServiceImpl();

        serviceProvider.publish(helloService, host, port);

        Thread.sleep(Integer.MAX_VALUE);

    }
}
