package com.saaavsaaa.client.zookeeper.operation;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.zookeeper.base.BaseOperation;
import com.saaavsaaa.client.zookeeper.strategy.UsualStrategy;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa
 */
public class DeleteCurrentBranchOperation extends BaseOperation {
    private final String key;
    
    public DeleteCurrentBranchOperation(final IProvider provider, final String key) {
        super(provider);
        this.key = key;
    }
    
    @Override
    protected boolean execute() throws KeeperException, InterruptedException {
        try {
            new UsualStrategy(provider).deleteCurrentBranch(key);
            return true;
        } catch (KeeperException.SessionExpiredException ee) {
            return false;
        }
    }
    
    @Override
    public String toString(){
        return String.format("DeleteCurrentBranchOperation key:%s", key);
    }
}
