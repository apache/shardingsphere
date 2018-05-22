package com.saaavsaaa.client.zookeeper.operation;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.section.Connection;
import com.saaavsaaa.client.zookeeper.base.BaseOperation;
import com.saaavsaaa.client.zookeeper.base.BaseProvider;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa
 */
public class UpdateOperation extends BaseOperation {
    private final String key;
    private final String value;
    
    public UpdateOperation(final IClient client, final String key, final String value) {
        super(client);
        this.key = key;
        this.value = value;
    }
    
    @Override
    protected void execute() throws KeeperException, InterruptedException {
        IProvider provider = new BaseProvider(client, false);
        provider.update(provider.getRealPath(key), value);
    }
    
    @Override
    public String toString(){
        return String.format("UpdateOperation key:%s,value:%s", key, value);
    }
}
