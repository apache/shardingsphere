package com.saaavsaaa.client.retry;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.section.Listener;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.zookeeper.ClientFactory;
import com.saaavsaaa.client.zookeeper.TestSupport;
import com.saaavsaaa.client.zookeeper.base.BaseClient;
import com.saaavsaaa.client.zookeeper.base.BaseProvider;
import com.saaavsaaa.client.zookeeper.strategy.StrategyType;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by aaa
 */
public class AsyncRetryCenterTest {
    private IProvider provider;
    private IClient client;
    
    @Before
    public void start() throws IOException, InterruptedException {
        provider = new BaseProvider(createClient(), false);
        AsyncRetryCenter.INSTANCE.init(new RetryPolicy(3, 3, 10));
        AsyncRetryCenter.INSTANCE.start();
    }
    
    protected IClient createClient() throws IOException, InterruptedException {
        ClientFactory creator = new ClientFactory();
        Listener listener = TestSupport.buildListener();
        client = creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).watch(listener).start();
        ((BaseClient)client).useExecStrategy(StrategyType.ASYNC_RETRY);
        return client;
    }
    
    @After
    public void stop(){
        
    }
    
    @Ignore
    @Test
    public void nothing(){
        
    }
    
    @Test
    public void create() throws InterruptedException, KeeperException {
        String key = "a";
        String value = "bbb11";
        AsyncRetryCenter.INSTANCE.add(new TestCreateCurrentOperation(client, key, value, CreateMode.PERSISTENT));
        Thread.sleep(1000);
        String path = PathUtil.getRealPath(TestSupport.ROOT, key);
        assert provider.checkExists(path);
        provider.deleteOnlyCurrent(path);
        provider.deleteOnlyCurrent(provider.getRealPath(TestSupport.ROOT));
        assert !provider.checkExists(path);
    }
}
