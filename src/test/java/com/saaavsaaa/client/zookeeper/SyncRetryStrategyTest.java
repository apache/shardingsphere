package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.retry.AsyncRetryCenter;
import com.saaavsaaa.client.retry.DelayRetryPolicy;
import com.saaavsaaa.client.retry.TestCallable;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.zookeeper.base.BaseClient;
import com.saaavsaaa.client.zookeeper.section.Listener;
import com.saaavsaaa.client.zookeeper.section.StrategyType;
import com.saaavsaaa.client.zookeeper.strategy.UsualStrategy;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by aaa
 */
public class SyncRetryStrategyTest extends UsualClientTest{
    private IProvider provider;
    
    @Before
    public void start() throws IOException, InterruptedException {
        testClient = createClient();
        provider = ((BaseClient)testClient).getStrategy().getProvider();
        AsyncRetryCenter.INSTANCE.init(new DelayRetryPolicy(3, 3, 10));
        AsyncRetryCenter.INSTANCE.start();
    }
    
    protected IClient createClient() throws IOException, InterruptedException {
        ClientFactory creator = new ClientFactory();
        Listener listener = TestSupport.buildListener();
        IClient client = creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).watch(listener).start();
        client.useExecStrategy(StrategyType.SYNC_RETRY);
        return client;
    }
    
    @Test
    public void createChild() throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        new UsualStrategy(provider).deleteCurrentBranch(key);
        TestCallable callable = new TestCallable(provider, DelayRetryPolicy.newNoInitDelayPolicy()) {
            @Override
            public void test() throws KeeperException, InterruptedException {
                testClient.useExecStrategy(StrategyType.USUAL);
                testClient.createAllNeedPath(key, "bbb11", CreateMode.PERSISTENT);
                testClient.useExecStrategy(StrategyType.SYNC_RETRY);
            }
        };
        callable.exec();
        
        assert getZooKeeper(testClient).exists(PathUtil.getRealPath(TestSupport.ROOT, key), false) != null;
        new UsualStrategy(provider).deleteCurrentBranch(key);
        assert getZooKeeper(testClient).exists(PathUtil.getRealPath(TestSupport.ROOT, key), false) == null;
    }
    
    @Test
    public void deleteBranch() throws KeeperException, InterruptedException {
        String keyB = "a/b/bb";
        String value = "bbb11";
        new UsualStrategy(provider).createAllNeedPath(keyB, value, CreateMode.PERSISTENT);
        assert getZooKeeper(testClient).exists(PathUtil.getRealPath(TestSupport.ROOT, keyB), false) != null;
        String keyC  = "a/c/cc";
        new UsualStrategy(provider).createAllNeedPath(keyC, "ccc11", CreateMode.PERSISTENT);
        assert getZooKeeper(testClient).exists(PathUtil.getRealPath(TestSupport.ROOT, keyC), false) != null;
        
        TestCallable callable = new TestCallable(provider, DelayRetryPolicy.newNoInitDelayPolicy()) {
            @Override
            public void test() throws KeeperException, InterruptedException {
                new UsualStrategy(provider).deleteCurrentBranch(keyC);
            }
        };
        callable.exec();
    }
    
    @Test
    public void isExisted() throws KeeperException, InterruptedException {
        super.isExisted(testClient);
    }
    
    @Test
    public void get() throws KeeperException, InterruptedException {
        super.get(testClient);
    }
    
    @Test
    public void asynGet() throws KeeperException, InterruptedException {
        super.asynGet(testClient);
    }
    
    @Test
    public void getChildrenKeys() throws KeeperException, InterruptedException {
        super.getChildrenKeys(testClient);
    }
    
    @Test
    public void persist() throws KeeperException, InterruptedException {
        super.persist(testClient);
    }
    
    @Test
    public void persistEphemeral() throws KeeperException, InterruptedException {
        super.persistEphemeral(testClient);
    }
    
    @Test
    public void delAllChildren() throws KeeperException, InterruptedException {
        super.delAllChildren(testClient);
    }
    
    @Test
    public void watch() throws KeeperException, InterruptedException {
        super.watch(testClient);
    }
    
    @Test
    public void close() throws Exception {
        super.close(testClient);
    }
}
