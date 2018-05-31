package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.retry.AsyncRetryCenter;
import com.saaavsaaa.client.retry.DelayRetryPolicy;
import com.saaavsaaa.client.zookeeper.section.Listener;
import com.saaavsaaa.client.zookeeper.base.BaseClient;
import com.saaavsaaa.client.zookeeper.section.StrategyType;
import org.junit.Before;

import java.io.IOException;

/**
 * Created by aaa
 */
public class AsyncRetryStrategyTest extends UsualClientTest{
    @Before
    public void start() throws IOException, InterruptedException {
        super.start();
        AsyncRetryCenter.INSTANCE.init(new DelayRetryPolicy(3, 3, 10));
        AsyncRetryCenter.INSTANCE.start();
    }
    
    @Override
    protected IClient createClient(final ClientFactory creator) throws IOException, InterruptedException {
        Listener listener = TestSupport.buildListener();
        IClient client = creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).watch(listener).start();
        client.useExecStrategy(StrategyType.ASYNC_RETRY);
        return client;
    }
}
