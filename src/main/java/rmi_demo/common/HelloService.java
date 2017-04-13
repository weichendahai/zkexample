package rmi_demo.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by weichen on 17/4/12.
 */
public interface HelloService extends Remote{

    String sayHello (String name) throws RemoteException;
}
