/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.reg.newzk.client.zookeeper.strategy;

import io.shardingsphere.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.orchestration.reg.newzk.client.retry.DelayRetryPolicy;
import io.shardingsphere.orchestration.reg.newzk.client.retry.RetryCallable;
import io.shardingsphere.orchestration.reg.newzk.client.retry.RetryResultCallable;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.util.List;

/**
 * Sync retry strategy.
 *
 * @author lidongbo
 */
@Getter(value = AccessLevel.PROTECTED)
public class SyncRetryStrategy extends UsualStrategy {
    
    private final DelayRetryPolicy delayRetryPolicy;
    
    public SyncRetryStrategy(final IProvider provider, final DelayRetryPolicy delayRetryPolicy) {
        super(provider);
        this.delayRetryPolicy = null == delayRetryPolicy ? DelayRetryPolicy.defaultDelayPolicy() : delayRetryPolicy;
    }

    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        RetryResultCallable<byte[]> retryCallable = new RetryResultCallable<byte[]>(getProvider(), delayRetryPolicy) {
            
            @Override
            public void call() throws KeeperException, InterruptedException {
                setResult(getProvider().getData(getProvider().getRealPath(key)));
            }
        };
        return retryCallable.getResult();
    }
    
    @Override
    public boolean checkExists(final String key) throws KeeperException, InterruptedException {
        RetryResultCallable<Boolean> retryCallable = new RetryResultCallable<Boolean>(getProvider(), delayRetryPolicy) {
            
            @Override
            public void call() throws KeeperException, InterruptedException {
                setResult(getProvider().exists(getProvider().getRealPath(key)));
            }
        };
        return retryCallable.getResult();
    }
    
    @Override
    public boolean checkExists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        RetryResultCallable<Boolean> retryCallable = new RetryResultCallable<Boolean>(getProvider(), delayRetryPolicy) {
            
            @Override
            public void call() throws KeeperException, InterruptedException {
                setResult(getProvider().exists(getProvider().getRealPath(key), watcher));
            }
        };
        return retryCallable.getResult();
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        RetryResultCallable<List<String>> retryCallable = new RetryResultCallable<List<String>>(getProvider(), delayRetryPolicy) {
            
            @Override
            public void call() throws KeeperException, InterruptedException {
                setResult(getProvider().getChildren(getProvider().getRealPath(key)));
            }
        };
        return retryCallable.getResult();
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        RetryCallable retryCallable = new RetryCallable(getProvider(), delayRetryPolicy) {
            
            @Override
            public void call() throws KeeperException, InterruptedException {
                getProvider().create(getProvider().getRealPath(key), value, createMode);
            }
        };
        retryCallable.exec();
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        RetryCallable retryCallable = new RetryCallable(getProvider(), delayRetryPolicy) {
            
            @Override
            public void call() throws KeeperException, InterruptedException {
                getProvider().update(getProvider().getRealPath(key), value);
            }
        };
        retryCallable.exec();
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        RetryCallable retryCallable = new RetryCallable(getProvider(), delayRetryPolicy) {
            
            @Override
            public void call() throws KeeperException, InterruptedException {
                getProvider().delete(getProvider().getRealPath(key));
            }
        };
        retryCallable.exec();
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        RetryCallable retryCallable = new RetryCallable(getProvider(), delayRetryPolicy) {
            
            @Override
            public void call() throws KeeperException, InterruptedException {
                new UsualStrategy(getProvider()).createAllNeedPath(key, value, createMode);
            }
        };
        retryCallable.exec();
    }
    
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        RetryCallable retryCallable = new RetryCallable(getProvider(), delayRetryPolicy) {
            
            @Override
            public void call() throws KeeperException, InterruptedException {
                new UsualStrategy(getProvider()).deleteAllChildren(key);
            }
        };
        retryCallable.exec();
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        RetryCallable retryCallable = new RetryCallable(getProvider(), delayRetryPolicy) {
            
            @Override
            public void call() throws KeeperException, InterruptedException {
                new UsualStrategy(getProvider()).deleteCurrentBranch(key);
            }
        };
        retryCallable.exec();
    }
}
