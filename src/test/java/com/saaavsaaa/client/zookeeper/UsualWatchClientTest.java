package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.utility.section.Listener;

import java.io.IOException;

/**
 * Created by aaa
 */
public class UsualWatchClientTest extends UsualClientTest {
    
    @Override
    protected Client createClient(final ClientFactory creator) throws IOException, InterruptedException {
        Listener listener = TestSupport.buildListener();
        return creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newUsualClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).watch(listener).start();
    }
}