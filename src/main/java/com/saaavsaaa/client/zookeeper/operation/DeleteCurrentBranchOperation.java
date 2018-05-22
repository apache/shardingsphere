package com.saaavsaaa.client.zookeeper.operation;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.section.Connection;
import com.saaavsaaa.client.zookeeper.base.BaseOperation;
import com.saaavsaaa.client.zookeeper.base.BaseProvider;
import com.saaavsaaa.client.zookeeper.strategy.UsualStrategy;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa
 */
public class DeleteCurrentBranchOperation extends BaseOperation {
    private final String key;
    
    public DeleteCurrentBranchOperation(final IClient client, final String key) {
        super(client);
        this.key = key;
    }
    
    @Override
    protected void execute() throws KeeperException, InterruptedException {
        new UsualStrategy(new BaseProvider(client, false)).deleteCurrentBranch(key);
    }
    
    @Override
    public String toString(){
        return String.format("DeleteCurrentBranchOperation key:%s", key);
    }
}
