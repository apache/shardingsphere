package com.saaavsaaa.client.zookeeper.strategy;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.retry.DelayRetryPolicy;
import com.saaavsaaa.client.zookeeper.section.Callable;
import com.saaavsaaa.client.zookeeper.section.ClientContext;
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
    protected final DelayRetryPolicy delayRetryPolicy;
    
    public SyncRetryStrategy(final IProvider provider, final DelayRetryPolicy delayRetryPolicy) {
        super(provider);
        if (delayRetryPolicy == null){
            logger.warn("Callable constructor context's delayRetryPolicy is null");
            this.delayRetryPolicy = DelayRetryPolicy.newNoInitDelayPolicy();
        } else {
            this.delayRetryPolicy = delayRetryPolicy;
        }
//        this.delayRetryPolicy = delayRetryPolicy == null? DelayRetryPolicy.newNoInitDelayPolicy() : delayRetryPolicy;
    }

    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        Callable<byte[]> callable = new Callable(provider, delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                setResult(provider.getData(provider.getRealPath(key)));
            }
        };
        return callable.getResult();
    }
    
    @Override
    public boolean checkExists(final String key) throws KeeperException, InterruptedException {
        Callable<Boolean> callable = new Callable(provider, delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                setResult(provider.exists(provider.getRealPath(key)));
            }
        };
        return callable.getResult();
    }
    
    @Override
    public boolean checkExists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        Callable<Boolean> callable = new Callable(provider, delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                setResult(provider.exists(provider.getRealPath(key), watcher));
            }
        };
        return callable.getResult();
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        Callable<List<String>> callable = new Callable(provider, delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                setResult(provider.getChildren(provider.getRealPath(key)));
            }
        };
        return callable.getResult();
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        Callable callable = new Callable(provider, delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                provider.create(provider.getRealPath(key), value, createMode);
            }
        };
        callable.exec();
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        Callable callable = new Callable(provider, delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                provider.update(provider.getRealPath(key), value);
            }
        };
        callable.exec();
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        Callable callable = new Callable(provider, delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                provider.delete(provider.getRealPath(key));
            }
        };
        callable.exec();
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        Callable callable = new Callable(provider, delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                new UsualStrategy(provider).createAllNeedPath(key, value, createMode);
            }
        };
        callable.exec();
    }
    
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        Callable callable = new Callable(provider, delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                new UsualStrategy(provider).deleteAllChildren(key);
            }
        };
        callable.exec();
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        Callable callable = new Callable(provider, delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                new UsualStrategy(provider).deleteCurrentBranch(key);
            }
        };
        callable.exec();
    }
}
