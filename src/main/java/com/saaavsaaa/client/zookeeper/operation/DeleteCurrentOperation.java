package com.saaavsaaa.client.zookeeper.operation;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.section.ClientContext;
import com.saaavsaaa.client.zookeeper.base.BaseOperation;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa
 */
public class DeleteCurrentOperation extends BaseOperation {
    private final String key;
    public DeleteCurrentOperation(final ClientContext context, final String key) {
        super(context);
        this.key = key;
    }
    
    @Override
    protected void execute() throws KeeperException, InterruptedException {
        IProvider provider = context.getProvider();
        provider.delete(provider.getRealPath(key));
    }
    
    @Override
    public String toString(){
        return String.format("DeleteCurrentOperation key:%s", key);
    }
}
