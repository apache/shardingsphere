package com.saaavsaaa.client.zookeeper.operation;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.zookeeper.base.BaseOperation;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa
 */
public class DeleteCurrentOperation extends BaseOperation {
    private final String key;
    public DeleteCurrentOperation(final IProvider provider, final String key) {
        super(provider);
        this.key = key;
    }
    
    @Override
    protected boolean execute() throws KeeperException, InterruptedException {
        try {
            provider.deleteOnlyCurrent(key);
            return true;
        } catch (KeeperException.SessionExpiredException ee) {
            return false;
        }
    }
    
    @Override
    public String toString(){
        return String.format("DeleteCurrentOperation key:%s", key);
    }
}
