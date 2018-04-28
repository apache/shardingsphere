package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.utility.section.Listener;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;

/**
 * Created by aaa
 */
public class CacheClientTest {
    private Client client = null;
    
    //    @Before
    public void start() throws IOException, InterruptedException {
        ClientFactory creator = new ClientFactory();
        client = creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newCacheClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).start();
    }
    
    @Before
    public void startWithWatch() throws IOException, InterruptedException {
        ClientFactory creator = new ClientFactory();
        Listener listener = TestSupport.buildListener();
        client = creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newCacheClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).watch(listener).start();
    }
    
    @After
    public void stop() throws InterruptedException {
        client.close();
        client = null;
    }
}
