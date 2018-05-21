package com.saaavsaaa.client.zookeeper.operation;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.section.Connection;
import com.saaavsaaa.client.zookeeper.base.BaseOperation;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa
 */
public class CreateCurrentOperation extends BaseOperation {
    private final String key;
    private final String value;
    private final CreateMode createMode;
    
    public CreateCurrentOperation(final IProvider provider, final String key, final String value, final CreateMode createMode) {
        super(provider);
        this.key = provider.getRealPath(key);
        this.value = value;
        this.createMode = createMode;
    }
    
    @Override
    public boolean execute() throws KeeperException, InterruptedException {
        try {
            provider.createCurrentOnly(key, value, createMode);
            return true;
        } catch (KeeperException ee) {
            Connection.check(ee);
            return false;
        }
    }
    
    @Override
    public String toString(){
        return String.format("CreateCurrentOperation key:%s,value:%s,createMode:%s", key, value, createMode.name());
    }
}
