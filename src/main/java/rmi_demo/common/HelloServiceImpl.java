package rmi_demo.common;

import rmi_demo.common.HelloService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by weichen on 17/4/12.
 */
public class HelloServiceImpl extends UnicastRemoteObject implements HelloService{

    public HelloServiceImpl() throws RemoteException {
    }

    //重写hello service
    public String sayHello(String name) throws RemoteException {
        return String.format("Hello %s", name);
    }
}
