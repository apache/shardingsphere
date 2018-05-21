package com.saaavsaaa.client.zookeeper.operation;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.section.Connection;
import com.saaavsaaa.client.zookeeper.base.BaseOperation;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa
 */
public class UpdateOperation extends BaseOperation {
    private final String key;
    private final String value;
    
    public UpdateOperation(final IProvider provider, final String key, final String value) {
        super(provider);
        this.key = key;
        this.value = value;
    }
    
    @Override
    protected boolean execute() throws KeeperException, InterruptedException {
        try {
            provider.update(key, value);
            return true;
        } catch (KeeperException ee) {
            Connection.check(ee);
            return false;
        }
    }
    
    @Override
    public String toString(){
        return String.format("UpdateOperation key:%s,value:%s", key, value);
    }
}
