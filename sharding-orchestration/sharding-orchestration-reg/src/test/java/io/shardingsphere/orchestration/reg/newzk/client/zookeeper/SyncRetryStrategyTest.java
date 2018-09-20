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

package io.shardingsphere.orchestration.reg.newzk.client.zookeeper;

import io.shardingsphere.orchestration.reg.newzk.client.action.IClient;
import io.shardingsphere.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.orchestration.reg.newzk.client.retry.DelayRetryPolicy;
import io.shardingsphere.orchestration.reg.newzk.client.retry.TestCallable;
import io.shardingsphere.orchestration.reg.newzk.client.retry.TestResultCallable;
import io.shardingsphere.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.base.TestSupport;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section.StrategyType;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.strategy.UsualStrategy;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class SyncRetryStrategyTest extends UsualClientTest {
    
    private IProvider provider;
    
    @Override
    protected IClient createClient(final ClientFactory creator) throws IOException, InterruptedException {
        final IClient client = creator.setClientNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL)
                .newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).start();
        client.useExecStrategy(StrategyType.SYNC_RETRY);
        provider = client.getExecStrategy().getProvider();
        return client;
    }
    
    @Test
    public void createChild() throws KeeperException, InterruptedException {
        final String key = "a/b/bb";
        new UsualStrategy(provider).deleteCurrentBranch(key);
        TestCallable callable = new TestCallable(provider, DelayRetryPolicy.defaultDelayPolicy()) {
            
            @Override
            public void test() throws KeeperException, InterruptedException {
                getTestClient().useExecStrategy(StrategyType.USUAL);
                getTestClient().createAllNeedPath(key, "bbb11", CreateMode.PERSISTENT);
                getTestClient().useExecStrategy(StrategyType.SYNC_RETRY);
            }
        };
        callable.exec();
        assertTrue(getTestClient().checkExists(key));
        new UsualStrategy(provider).deleteCurrentBranch(key);
        assertFalse(getTestClient().checkExists(key));
    }
    
    @Test
    public void deleteBranch() throws KeeperException, InterruptedException {
        final String keyB = "a/b/bb";
        final String value = "bbb11";
        getTestClient().useExecStrategy(StrategyType.USUAL);
        getTestClient().createAllNeedPath(keyB, value, CreateMode.PERSISTENT);
        getTestClient().useExecStrategy(StrategyType.SYNC_RETRY);
    
        assertTrue(getTestClient().checkExists(keyB));
        final String keyC = "a/c/cc";
        new UsualStrategy(provider).createAllNeedPath(keyC, "ccc11", CreateMode.PERSISTENT);
        assertTrue(getTestClient().checkExists(keyC));
        
        TestCallable callable = getDeleteBranch(keyC);
        callable.exec();
        assertFalse(getTestClient().checkExists(keyC));
        assertTrue(getTestClient().checkExists("a"));
    
        callable = getDeleteBranch(keyB);
        callable.exec();
        assertFalse(getTestClient().checkExists(PathUtil.checkPath(TestSupport.ROOT)));
        getTestClient().createAllNeedPath(keyB, "bbb11", CreateMode.PERSISTENT);
        assertTrue(getTestClient().checkExists(keyB));
        
        callable.exec();
        assertFalse(getTestClient().checkExists(PathUtil.checkPath(TestSupport.ROOT)));
    }
    
    private TestCallable getDeleteBranch(final String key) {
        return new TestCallable(provider, DelayRetryPolicy.defaultDelayPolicy()) {
            
            @Override
            public void test() throws KeeperException, InterruptedException {
                getTestClient().useExecStrategy(StrategyType.USUAL);
                getTestClient().deleteCurrentBranch(key);
                getTestClient().useExecStrategy(StrategyType.SYNC_RETRY);
            }
        };
    }
    
    @Test
    public void isExisted() throws KeeperException, InterruptedException {
        final String key = "a/b/bb";
        getTestClient().useExecStrategy(StrategyType.USUAL);
        getTestClient().createAllNeedPath(key, "", CreateMode.PERSISTENT);
        getTestClient().useExecStrategy(StrategyType.SYNC_RETRY);
    
        TestResultCallable<Boolean> callable = new TestResultCallable<Boolean>(provider, DelayRetryPolicy.defaultDelayPolicy()) {
            
            @Override
            public void test() throws KeeperException, InterruptedException {
                setResult(provider.exists(provider.getRealPath(key)));
            }
        };
        assertTrue(callable.getResult());
    
        getTestClient().useExecStrategy(StrategyType.USUAL);
        getTestClient().deleteCurrentBranch(key);
        getTestClient().useExecStrategy(StrategyType.SYNC_RETRY);
    }
    
    @Test
    public void get() throws KeeperException, InterruptedException {
        final String key = "a/b";
        getTestClient().useExecStrategy(StrategyType.USUAL);
        getTestClient().createAllNeedPath(key, "bbb11", CreateMode.PERSISTENT);
        getTestClient().useExecStrategy(StrategyType.SYNC_RETRY);
    
        TestResultCallable<String> callable = getData("a");
        assertThat(callable.getResult(), is(""));
        callable = getData(key);
        assertThat(callable.getResult(), is("bbb11"));
        
        getTestClient().useExecStrategy(StrategyType.USUAL);
        getTestClient().deleteCurrentBranch(key);
        getTestClient().useExecStrategy(StrategyType.SYNC_RETRY);
    }
    
    private TestResultCallable<String> getData(final String key) {
        return new TestResultCallable<String>(provider, DelayRetryPolicy.defaultDelayPolicy()) {
            
            @Override
            public void test() throws KeeperException, InterruptedException {
                setResult(new String(provider.getData(provider.getRealPath(key))));
            }
        };
    }
    
    @Test
    public void getChildrenKeys() throws KeeperException, InterruptedException {
        final String key = "a/b";
        final String current = "a";
        
        getTestClient().useExecStrategy(StrategyType.USUAL);
        getTestClient().createAllNeedPath(key, "", CreateMode.PERSISTENT);
        getTestClient().createAllNeedPath("a/c", "", CreateMode.PERSISTENT);
        getTestClient().useExecStrategy(StrategyType.SYNC_RETRY);
    
        final TestResultCallable<List<String>> callable = new TestResultCallable<List<String>>(provider, DelayRetryPolicy.defaultDelayPolicy()) {
            
            @Override
            public void test() throws KeeperException, InterruptedException {
                setResult(provider.getChildren(provider.getRealPath(current)));
            }
        };
        final List<String> result = callable.getResult();
        Collections.sort(result, new Comparator<String>() {
            public int compare(final String o1, final String o2) {
                return o2.compareTo(o1);
            }
        });
        assertThat(result.get(0), is("c"));
        assertThat(result.get(1), is("b"));
        
        getTestClient().useExecStrategy(StrategyType.USUAL);
        getTestClient().deleteAllChildren(PathUtil.checkPath(TestSupport.ROOT));
        getTestClient().useExecStrategy(StrategyType.SYNC_RETRY);
    }
    
    @Test
    public void update() throws KeeperException, InterruptedException {
        final String key = "a";
        final String value = "aa";
        final String newValue = "aaa";
        getTestClient().deleteCurrentBranch(key);
        getTestClient().createAllNeedPath(key, value, CreateMode.PERSISTENT);
        assertThat(getTestClient().getDataString(key), is(value));
    
        final TestCallable callable = new TestCallable(provider, DelayRetryPolicy.defaultDelayPolicy()) {
            
            @Override
            public void test() throws KeeperException, InterruptedException {
                provider.update(provider.getRealPath(key), newValue);
            }
        };
        callable.exec();
        assertThat(getTestClient().getDataString(key), is(newValue));
        getTestClient().deleteCurrentBranch(key);
    }
    
    @Test
    public void delAllChildren() throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        getTestClient().createAllNeedPath(key, "bb", CreateMode.PERSISTENT);
        key = "a/c/cc";
        getTestClient().createAllNeedPath(key, "cc", CreateMode.PERSISTENT);
        assertNotNull(getZooKeeper(getTestClient()).exists(PathUtil.getRealPath(TestSupport.ROOT, key), false));
    
        TestCallable callable = new TestCallable(provider, DelayRetryPolicy.defaultDelayPolicy()) {
            @Override
            public void test() throws KeeperException, InterruptedException {
                new UsualStrategy(provider).deleteAllChildren("a");
            }
        };
        callable.exec();
        assertNull(getZooKeeper(getTestClient()).exists(PathUtil.getRealPath(TestSupport.ROOT, key), false));
        assertNotNull(getZooKeeper(getTestClient()).exists(PathUtil.checkPath(TestSupport.ROOT), false));
        super.deleteRoot(getTestClient());
    }
}
