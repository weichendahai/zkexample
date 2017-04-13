package zk_lock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by weichen on 17/4/13.
 */
public class tt {



    public static void main(String[] args) {

        // create an empty array list with an initial capacity
        ArrayList<String> arrlist = new ArrayList<String>(5);

        // use add() method to add values in the list
        arrlist.add("G");
        arrlist.add("E");
        arrlist.add("F");
        arrlist.add("M");

        System.out.println("Size of list: " + arrlist.size());

        // let us print all the values available in list
        for (String value : arrlist) {
            System.out.println("Value = " + value);
        }

        // retrieving the index of element "E"
        int retval=arrlist.indexOf("E");
        System.out.println("The element E is at index " + retval);

//        System.exit(0);


        List<String> subNodes = new ArrayList<String>();

        for (int i = 0; i < 10; i++) {
            subNodes.add("/disLocks/sub000000013" + i);
        }

        String GROUP_PATH = "/disLocks";
        String SUB_PATH = "/disLocks/sub";

        System.out.println(subNodes);
        Collections.sort(subNodes);

        System.out.println(subNodes);


//        String selfPath = "/disLocks/sub0000000131";
//        System.out.println("GROUP_PATH.length: " + GROUP_PATH.length());
//
//        System.out.println("selfPath.substring: " + selfPath.substring(GROUP_PATH.length() + 1));


        for (String node:subNodes) {
//            int index = subNodes.indexOf(node.substring(GROUP_PATH.length() + 1));
            int index = subNodes.indexOf(node);

            System.out.println("node: " + node + ", in list index:" + index);
        }



    }
}
