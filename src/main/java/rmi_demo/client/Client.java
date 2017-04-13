package rmi_demo.client;

import rmi_demo.common.HelloService;

/**
 * Created by weichen on 17/4/12.
 */
public class Client {

    public static void main(String[] args) throws  Exception{

        ServiceConsumer consumer = new ServiceConsumer();
        /*zk 测试*/
        while (true) {
            HelloService helloService = consumer.lookup();
            String result = helloService.sayHello("jajaajaj");
            System.out.println(result);

            Thread.sleep(3000);
        }
    }
}
