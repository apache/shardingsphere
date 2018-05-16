package com.saaavsaaa.client.zookeeper.operation;

import com.saaavsaaa.client.zookeeper.base.BaseOperation;
import com.saaavsaaa.client.zookeeper.base.BaseProvider;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa
 */
public class CreateCurrentOperation extends BaseOperation {
    private final String key;
    private final String value;
    private final CreateMode createMode;
    
    public CreateCurrentOperation(BaseProvider baseProvider, String key, String value, CreateMode createMode) {
        super(baseProvider);
        this.key = key;
        this.value = value;
        this.createMode = createMode;
    }
    
    @Override
    public boolean execute() throws KeeperException, InterruptedException {
        try {
            provider.createCurrentOnly(key, value, createMode);
            return true;
        } catch (KeeperException.SessionExpiredException ee) {
            return false;
        }
    }
}
