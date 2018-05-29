package com.saaavsaaa.client.retry;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.zookeeper.section.ClientContext;
import com.saaavsaaa.client.zookeeper.section.Listener;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.ClientFactory;
import com.saaavsaaa.client.zookeeper.TestSupport;
import com.saaavsaaa.client.zookeeper.base.BaseClient;
import com.saaavsaaa.client.zookeeper.strategy.StrategyType;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.junit.After;
import org.junit.Before;
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
        client = createClient();
        provider = ((BaseClient)client).getStrategy().getProvider();
        AsyncRetryCenter.INSTANCE.init(new DelayRetryPolicy(3, 3, 10));
        AsyncRetryCenter.INSTANCE.start();
    }
    
    protected IClient createClient() throws IOException, InterruptedException {
        ClientFactory creator = new ClientFactory();
        Listener listener = TestSupport.buildListener();
        IClient client = creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).watch(listener).start();
        client.useExecStrategy(StrategyType.ASYNC_RETRY);
        return client;
    }
    
    @After
    public void stop(){
        
    }
    
    @Test
    public void create() throws InterruptedException, KeeperException {
        String key = "a";
        String value = "bbb11";
        if (!provider.exists("/" + TestSupport.ROOT)) {
            System.out.println("exist root");
            provider.create("/" + TestSupport.ROOT, Constants.NOTHING_VALUE, CreateMode.PERSISTENT);
        }
        AsyncRetryCenter.INSTANCE.add(new TestCreateCurrentOperation(provider, key, value, CreateMode.PERSISTENT));
        Thread.sleep(2000);
        String path = PathUtil.getRealPath(TestSupport.ROOT, key);
        assert provider.exists(path);
//        assert client.checkExists(path);
        provider.delete(path);
        provider.delete(provider.getRealPath(TestSupport.ROOT));
        assert !provider.exists(path);
    }
}
