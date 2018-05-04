package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.utility.section.Listener;

import java.io.IOException;

/**
 * Created by aaa on 18-5-2.
 */
public class CacheWathClientTest extends UsualClientTest {
    @Override
    protected Client createClient(ClientFactory creator) throws IOException, InterruptedException {
        Listener listener = TestSupport.buildListener();
        return creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newUsualClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).watch(listener).start();
    }
}
