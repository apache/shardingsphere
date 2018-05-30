package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.retry.AsyncRetryCenter;
import com.saaavsaaa.client.retry.DelayRetryPolicy;
import com.saaavsaaa.client.retry.TestCallable;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.zookeeper.base.BaseClient;
import com.saaavsaaa.client.zookeeper.section.Callable;
import com.saaavsaaa.client.zookeeper.section.Listener;
import com.saaavsaaa.client.zookeeper.section.StrategyType;
import com.saaavsaaa.client.zookeeper.strategy.UsualStrategy;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        testClient.useExecStrategy(StrategyType.USUAL);
        testClient.createAllNeedPath(keyB, value, CreateMode.PERSISTENT);
        testClient.useExecStrategy(StrategyType.SYNC_RETRY);
        
        assert getZooKeeper(testClient).exists(PathUtil.getRealPath(TestSupport.ROOT, keyB), false) != null;
        String keyC  = "a/c/cc";
        new UsualStrategy(provider).createAllNeedPath(keyC, "ccc11", CreateMode.PERSISTENT);
        assert getZooKeeper(testClient).exists(PathUtil.getRealPath(TestSupport.ROOT, keyC), false) != null;
        
        TestCallable callable = getDeleteBranch(keyC);
        callable.exec();
    
        assert getZooKeeper(testClient).exists(PathUtil.getRealPath(TestSupport.ROOT, keyC), false) == null;
        assert getZooKeeper(testClient).exists(PathUtil.getRealPath(TestSupport.ROOT, "a"), false) != null;
    
        callable = getDeleteBranch(keyB);
        callable.exec();
        
        assert getZooKeeper(testClient).exists(PathUtil.checkPath(TestSupport.ROOT), false) == null;
        testClient.createAllNeedPath(keyB, "bbb11", CreateMode.PERSISTENT);
        assert getZooKeeper(testClient).exists(PathUtil.getRealPath(TestSupport.ROOT, keyB), false) != null;
        
        callable.exec();
        
        assert getZooKeeper(testClient).exists(PathUtil.checkPath(TestSupport.ROOT), false) == null;
    }
    
    private TestCallable getDeleteBranch(final String key){
        TestCallable callable = new TestCallable(provider, DelayRetryPolicy.newNoInitDelayPolicy()) {
            @Override
            public void test() throws KeeperException, InterruptedException {
                testClient.useExecStrategy(StrategyType.USUAL);
                testClient.deleteCurrentBranch(key);
                testClient.useExecStrategy(StrategyType.SYNC_RETRY);
            }
        };
        return callable;
    }
    
    @Test
    public void isExisted() throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        testClient.useExecStrategy(StrategyType.USUAL);
        testClient.createAllNeedPath(key, "", CreateMode.PERSISTENT);
        testClient.useExecStrategy(StrategyType.SYNC_RETRY);
    
        TestCallable callable = new TestCallable(provider, DelayRetryPolicy.newNoInitDelayPolicy()) {
            @Override
            public void test() throws KeeperException, InterruptedException {
                setResult(provider.exists(provider.getRealPath(key)));
            }
        };
        System.out.println(callable.getResult());
        assert callable.getResult().equals(true);
    
        testClient.useExecStrategy(StrategyType.USUAL);
        testClient.deleteCurrentBranch(key);
        testClient.useExecStrategy(StrategyType.SYNC_RETRY);
    }
    
    @Test
    public void get() throws KeeperException, InterruptedException {
        String key = "a/b";
        testClient.useExecStrategy(StrategyType.USUAL);
        testClient.createAllNeedPath(key, "bbb11", CreateMode.PERSISTENT);
        testClient.useExecStrategy(StrategyType.SYNC_RETRY);
        
        TestCallable callable = getData("a");
        assert callable.getResult().equals("");
        callable = getData(key);
        assert callable.getResult().equals("bbb11");
        
        testClient.useExecStrategy(StrategyType.USUAL);
        testClient.deleteCurrentBranch(key);
        testClient.useExecStrategy(StrategyType.SYNC_RETRY);
    }
    
    private TestCallable getData(final String key){
        TestCallable callable = new TestCallable(provider, DelayRetryPolicy.newNoInitDelayPolicy()) {
            @Override
            public void test() throws KeeperException, InterruptedException {
                setResult(new String(provider.getData(provider.getRealPath(key))));
            }
        };
        return callable;
    }
    
    @Test
    public void getChildrenKeys() throws KeeperException, InterruptedException {
        String key = "a/b";
        String current = "a";
        
        testClient.useExecStrategy(StrategyType.USUAL);
        testClient.createAllNeedPath(key, "", CreateMode.PERSISTENT);
        testClient.createAllNeedPath("a/c", "", CreateMode.PERSISTENT);
        testClient.useExecStrategy(StrategyType.SYNC_RETRY);
    
        TestCallable callable = new TestCallable(provider, DelayRetryPolicy.newNoInitDelayPolicy()) {
            @Override
            public void test() throws KeeperException, InterruptedException {
                setResult(provider.getChildren(provider.getRealPath(current)));
            }
        };
        List<String> result = (List<String>) callable.getResult();
        Collections.sort(result, new Comparator<String>() {
            public int compare(final String o1, final String o2) {
                return o2.compareTo(o1);
            }
        });
        assert result.get(0).equals("c");
        assert result.get(1).equals("b");
        
        testClient.useExecStrategy(StrategyType.USUAL);
        testClient.deleteCurrentBranch(key);
        testClient.useExecStrategy(StrategyType.SYNC_RETRY);
    }
    
    @Test
    public void update() throws KeeperException, InterruptedException {
        String key = "a";
        String value = "aa";
        String newValue = "aaa";
        testClient.deleteCurrentBranch(key);
        testClient.createAllNeedPath(key, value, CreateMode.PERSISTENT);
        String data = testClient.getDataString(key);
        System.out.println(data);
        assert data.equals(value);
        
        TestCallable callable = new TestCallable(provider, DelayRetryPolicy.newNoInitDelayPolicy()) {
            @Override
            public void test() throws KeeperException, InterruptedException {
                provider.update(provider.getRealPath(key), newValue);
            }
        };
        callable.exec();
    
        assert testClient.getDataString(key).equals(newValue);
        testClient.deleteCurrentBranch(key);
    }
    
    @Test
    public void delAllChildren() throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        testClient.createAllNeedPath(key, "bb", CreateMode.PERSISTENT);
        key = "a/c/cc";
        testClient.createAllNeedPath(key, "cc", CreateMode.PERSISTENT);
        System.out.println(getZooKeeper(testClient).exists(PathUtil.getRealPath(TestSupport.ROOT, "a"), null).getNumChildren()); // nearest children count
        assert getZooKeeper(testClient).exists(PathUtil.getRealPath(TestSupport.ROOT, key), false) != null;
    
        TestCallable callable = new TestCallable(provider, DelayRetryPolicy.newNoInitDelayPolicy()) {
            @Override
            public void test() throws KeeperException, InterruptedException {
                new UsualStrategy(provider).deleteAllChildren("a");
            }
        };
        callable.exec();
        
        assert getZooKeeper(testClient).exists(PathUtil.getRealPath(TestSupport.ROOT, key), false) == null;
        assert getZooKeeper(testClient).exists("/" + TestSupport.ROOT, false) != null;
        super.deleteRoot(testClient);
    }
}
