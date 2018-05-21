package com.saaavsaaa.client.zookeeper.operation;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.section.Connection;
import com.saaavsaaa.client.zookeeper.base.BaseOperation;
import com.saaavsaaa.client.zookeeper.strategy.UsualStrategy;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa
 */
public class DeleteAllChildrenOperation extends BaseOperation {
    private final String key;
    
    public DeleteAllChildrenOperation(final IProvider provider, final String key) {
        super(provider);
        this.key = key;
    }
    
    @Override
    protected boolean execute() throws KeeperException, InterruptedException {
        try {
            new UsualStrategy(provider).deleteAllChildren(key);
            return true;
        } catch (KeeperException ee) {
            Connection.check(ee);
            return false;
        }
    }
    
    @Override
    public String toString(){
        return String.format("DeleteAllChildrenOperation key:%s", key);
    }
}
