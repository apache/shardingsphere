package com.saaavsaaa.client.zookeeper.strategy;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.retry.DelayRetryPolicy;
import com.saaavsaaa.client.retry.RetryCount;
import com.saaavsaaa.client.section.Callable;
import com.saaavsaaa.client.section.ClientContext;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by aaa
 */
public class SyncRetryStrategy extends UsualStrategy{
    private static final Logger logger = LoggerFactory.getLogger(SyncRetryStrategy.class);
    protected final ClientContext context;
    
    public SyncRetryStrategy(final ClientContext context) {
        super(context.getProvider());
        this.context = context;
    }

    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        Callable<byte[]> callable = new Callable(context) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                setResult(provider.getData(provider.getRealPath(key)));
            }
        };
        return callable.getResult();
    }
    
    @Override
    public boolean checkExists(final String key) throws KeeperException, InterruptedException {
        Callable<Boolean> callable = new Callable(context) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                setResult(provider.exists(provider.getRealPath(key)));
            }
        };
        return callable.getResult();
    }
    
    @Override
    public boolean checkExists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        Callable<Boolean> callable = new Callable(context) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                setResult(provider.exists(provider.getRealPath(key), watcher));
            }
        };
        return callable.getResult();
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        Callable<List<String>> callable = new Callable(context) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                setResult(provider.getChildren(provider.getRealPath(key)));
            }
        };
        return callable.getResult();
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        String path = provider.getRealPath(key);
        Callable callable = new Callable(context) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                provider.create(path, value, createMode);
            }
        };
        callable.exec();
    }
}
