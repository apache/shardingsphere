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
import io.shardingsphere.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.base.BaseClientTest;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.base.TestSupport;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section.StrategyType;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.strategy.AsyncRetryStrategy;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.strategy.ContentionStrategy;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.strategy.SyncRetryStrategy;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.strategy.TransactionContendStrategy;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.strategy.UsualStrategy;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.transaction.BaseTransaction;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class UsualClientTest extends BaseClientTest {
    
    @Override
    protected IClient createClient(final ClientFactory creator) throws IOException, InterruptedException {
        return creator.setClientNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL)
                .newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).start();
    }
    
    @Test
    public void assertUseExecStrategy() {
        getTestClient().useExecStrategy(StrategyType.CONTEND);
        assertThat(getTestClient().getExecStrategy().getClass().getName(), is(ContentionStrategy.class.getName()));
        getTestClient().useExecStrategy(StrategyType.TRANSACTION_CONTEND);
        assertThat(getTestClient().getExecStrategy().getClass().getName(), is(TransactionContendStrategy.class.getName()));
        getTestClient().useExecStrategy(StrategyType.SYNC_RETRY);
        assertThat(getTestClient().getExecStrategy().getClass().getName(), is(SyncRetryStrategy.class.getName()));
        getTestClient().useExecStrategy(StrategyType.ASYNC_RETRY);
        assertThat(getTestClient().getExecStrategy().getClass().getName(), is(AsyncRetryStrategy.class.getName()));
        getTestClient().useExecStrategy(StrategyType.USUAL);
        assertThat(getTestClient().getExecStrategy().getClass().getName(), is(UsualStrategy.class.getName()));
    }
    
    @Test
    public void assertGetData() throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        String value = "bbb11";
        getTestClient().createAllNeedPath(key, value, CreateMode.PERSISTENT);
        assertThat(getTestClient().getDataString(key), is(value));
        getTestClient().deleteCurrentBranch(key);
    }
    
    @Test
    public void assertCreateRoot() throws KeeperException, InterruptedException {
        super.createRoot(getTestClient());
    }
    
    @Test
    public void assertCreateChild() throws KeeperException, InterruptedException {
        super.createChild(getTestClient());
    }
    
    @Test
    public void assertDeleteBranch() throws KeeperException, InterruptedException {
        super.deleteBranch(getTestClient());
    }
    
    @Test
    public void assertExisted() throws KeeperException, InterruptedException {
        super.isExisted(getTestClient());
    }
    
    @Test
    public void assertGet() throws KeeperException, InterruptedException {
        super.get(getTestClient());
    }
    
    @Test
    public void assertAsyncGet() throws KeeperException, InterruptedException {
        super.asyncGet(getTestClient());
    }
    
    @Test
    public void assertGetChildrenKeys() throws KeeperException, InterruptedException {
        super.getChildrenKeys(getTestClient());
    }
    
    @Test
    public void assertPersist() throws KeeperException, InterruptedException {
        super.persist(getTestClient());
    }
    
    @Test
    public void assertPersistEphemeral() throws KeeperException, InterruptedException {
        super.persistEphemeral(getTestClient());
    }
    
    @Test
    public void assertDelAllChildren() throws KeeperException, InterruptedException {
        super.delAllChildren(getTestClient());
    }
    
    @Test
    public void assertWatch() throws KeeperException, InterruptedException {
        super.watch(getTestClient());
    }
    
    @Test
    public void assertWatchRegister() throws KeeperException, InterruptedException {
        super.watchRegister(getTestClient());
    }
    
    @Test
    public void assertClose() {
        super.close(getTestClient());
    }
    
    @Test
    public void assertDeleteOnlyCurrent() throws KeeperException, InterruptedException {
        String key = "key";
        String value = "value";
        getTestClient().createCurrentOnly(key, value, CreateMode.PERSISTENT);
        assertThat(getTestClient().getDataString(key), is(value));
        assertTrue(getTestClient().checkExists(key));
        getTestClient().deleteOnlyCurrent(key);
        assertFalse(getTestClient().checkExists(key));
        deleteRoot(getTestClient());
    }
    
    @Test
    public void assertTransaction() throws KeeperException, InterruptedException {
        String key = "key";
        String value = "value";
        BaseTransaction transaction = getTestClient().transaction();
        getTestClient().createCurrentOnly(key, value, CreateMode.PERSISTENT);
        transaction.setData(key, value.getBytes(ZookeeperConstants.UTF_8));
        transaction.commit();
        assertThat(getTestClient().getDataString(key), is(value));
        getTestClient().deleteOnlyCurrent(key);
        deleteRoot(getTestClient());
    }
}
