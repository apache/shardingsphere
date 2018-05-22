package com.saaavsaaa.client.zookeeper.operation;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.section.Connection;
import com.saaavsaaa.client.zookeeper.base.BaseOperation;
import com.saaavsaaa.client.zookeeper.base.BaseProvider;
import com.saaavsaaa.client.zookeeper.strategy.UsualStrategy;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa
 */
public class CreateAllNeedOperation extends BaseOperation {
    private final String key;
    private final String value;
    private final CreateMode createMode;
    
    public CreateAllNeedOperation(final IClient client, final String key, final String value, final CreateMode createMode) {
        super(client);
        this.key = key;
        this.value = value;
        this.createMode = createMode;
    }
    
    @Override
    protected void execute() throws KeeperException, InterruptedException {
        new UsualStrategy(new BaseProvider(client, false)).createAllNeedPath(key, value, createMode);
    }
    
    @Override
    public String toString(){
        return String.format("CreateAllNeedOperation key:%s,value:%s,createMode:%s", key, value, createMode.name());
    }
}
