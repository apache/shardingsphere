package com.saaavsaaa.client.retry;

import com.saaavsaaa.client.section.ClientContext;
import com.saaavsaaa.client.zookeeper.base.BaseProvider;
import com.saaavsaaa.client.zookeeper.operation.CreateCurrentOperation;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa
 */
public class TestCreateCurrentOperation extends CreateCurrentOperation {
    private int count = 0;
    
    public TestCreateCurrentOperation(final ClientContext context, String key, String value, CreateMode createMode) {
        super(context, key, value, createMode);
    }
    
    @Override
    public void execute() throws KeeperException, InterruptedException {
        if (count < 2){
            count++;
//            throw new KeeperException.SessionExpiredException();
//            throw new KeeperException.ConnectionLossException();
    
            System.out.println("test injectSessionExpiration==================================================" + count);
            ((BaseProvider)context.getProvider()).getZooKeeper().getTestable().injectSessionExpiration();
        }
        System.out.println("test ================================================" + count);
        super.execute();
    }
}
