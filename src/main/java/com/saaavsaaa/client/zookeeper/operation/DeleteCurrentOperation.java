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
public class DeleteCurrentOperation extends BaseOperation {
    private final String key;
    public DeleteCurrentOperation(final IClient client, final String key) {
        super(client);
        this.key = key;
    }
    
    @Override
    protected void execute() throws KeeperException, InterruptedException {
        IProvider provider = new BaseProvider(client, false);
        provider.deleteOnlyCurrent(provider.getRealPath(key));
    }
    
    @Override
    public String toString(){
        return String.format("DeleteCurrentOperation key:%s", key);
    }
}
