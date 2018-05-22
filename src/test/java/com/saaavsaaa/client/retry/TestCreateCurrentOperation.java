package com.saaavsaaa.client.retry;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.section.Connection;
import com.saaavsaaa.client.zookeeper.operation.CreateCurrentOperation;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa
 */
public class TestCreateCurrentOperation extends CreateCurrentOperation {
    private int count = 0;
    
    public TestCreateCurrentOperation(IProvider provider, String key, String value, CreateMode createMode) {
        super(provider, key, value, createMode);
    }
    
    @Override
    public boolean execute() throws KeeperException, InterruptedException {
        if (count < 2){
            count++;
            return false;
        }
        super.execute();
        return true;
    }
}
