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
public class DeleteAllChildrenOperation extends BaseOperation {
    private final String key;
    
    public DeleteAllChildrenOperation(final IClient client, final String key) {
        super(client);
        this.key = key;
    }
    
    @Override
    protected void execute() throws KeeperException, InterruptedException {
        new UsualStrategy(new BaseProvider(client, false)).deleteAllChildren(key);
    }
    
    @Override
    public String toString(){
        return String.format("DeleteAllChildrenOperation key:%s", key);
    }
}
