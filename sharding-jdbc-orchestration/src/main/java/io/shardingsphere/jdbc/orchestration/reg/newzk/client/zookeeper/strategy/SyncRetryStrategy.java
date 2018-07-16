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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.strategy;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry.DelayRetryPolicy;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry.Callable;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/*
 * sync retry strategy
 *
 * @author lidongbo
 */
public class SyncRetryStrategy extends UsualStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncRetryStrategy.class);
    
    @Getter(value = AccessLevel.PROTECTED)
    private final DelayRetryPolicy delayRetryPolicy;
    
    public SyncRetryStrategy(final IProvider provider, final DelayRetryPolicy delayRetryPolicy) {
        super(provider);
        if (delayRetryPolicy == null) {
            LOGGER.info("Callable constructor context's delayRetryPolicy is null");
            this.delayRetryPolicy = DelayRetryPolicy.newNoInitDelayPolicy();
        } else {
            this.delayRetryPolicy = delayRetryPolicy;
        }
    }

    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        Callable<byte[]> callable = new Callable<byte[]>(getProvider(), delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                setResult(getProvider().getData(getProvider().getRealPath(key)));
            }
        };
        return callable.getResult();
    }
    
    @Override
    public boolean checkExists(final String key) throws KeeperException, InterruptedException {
        Callable<Boolean> callable = new Callable<Boolean>(getProvider(), delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                setResult(getProvider().exists(getProvider().getRealPath(key)));
            }
        };
        return callable.getResult();
    }
    
    @Override
    public boolean checkExists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        Callable<Boolean> callable = new Callable<Boolean>(getProvider(), delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                setResult(getProvider().exists(getProvider().getRealPath(key), watcher));
            }
        };
        return callable.getResult();
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        Callable<List<String>> callable = new Callable<List<String>>(getProvider(), delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                setResult(getProvider().getChildren(getProvider().getRealPath(key)));
            }
        };
        return callable.getResult();
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        Callable callable = new Callable(getProvider(), delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                getProvider().create(getProvider().getRealPath(key), value, createMode);
            }
        };
        callable.exec();
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        Callable callable = new Callable(getProvider(), delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                getProvider().update(getProvider().getRealPath(key), value);
            }
        };
        callable.exec();
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        Callable callable = new Callable(getProvider(), delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                getProvider().delete(getProvider().getRealPath(key));
            }
        };
        callable.exec();
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        Callable callable = new Callable(getProvider(), delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                new UsualStrategy(getProvider()).createAllNeedPath(key, value, createMode);
            }
        };
        callable.exec();
    }
    
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        Callable callable = new Callable(getProvider(), delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                new UsualStrategy(getProvider()).deleteAllChildren(key);
            }
        };
        callable.exec();
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        Callable callable = new Callable(getProvider(), delayRetryPolicy) {
            @Override
            public void call() throws KeeperException, InterruptedException {
                new UsualStrategy(getProvider()).deleteCurrentBranch(key);
            }
        };
        callable.exec();
    }
}
