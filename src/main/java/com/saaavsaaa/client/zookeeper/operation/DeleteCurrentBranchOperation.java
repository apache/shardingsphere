package com.saaavsaaa.client.zookeeper.operation;

import com.saaavsaaa.client.section.ClientContext;
import com.saaavsaaa.client.zookeeper.base.BaseOperation;
import com.saaavsaaa.client.zookeeper.strategy.UsualStrategy;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa
 */
public class DeleteCurrentBranchOperation extends BaseOperation {
    private final String key;
    
    public DeleteCurrentBranchOperation(final ClientContext context, final String key) {
        super(context);
        this.key = key;
    }
    
    @Override
    protected void execute() throws KeeperException, InterruptedException {
        new UsualStrategy(context.getProvider()).deleteCurrentBranch(key);
    }
    
    @Override
    public String toString(){
        return String.format("DeleteCurrentBranchOperation key:%s", key);
    }
}
