package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.untils.Listener;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;

/**
 * Created by aaa on 18-4-26.
 */
public class BaseClientTest {
    protected Client client = null;
    
    @Before
    public void start() throws IOException, InterruptedException {
        ClientFactory creator = new ClientFactory();
//        client = createClient(creator);
        client = createWatchClient(creator);
    }
    
    protected Client createClient(ClientFactory creator) throws IOException, InterruptedException {
        return creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newUsualClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).start();
    }
    
    protected Client createWatchClient(ClientFactory creator) throws IOException, InterruptedException {
        Listener listener = TestSupport.buildListener();
        return creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newUsualClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).watch(listener).start();
    }
    
    @After
    public void stop() throws InterruptedException {
        client.close();
        client = null;
    }
}
