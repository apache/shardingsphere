package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.utility.section.Listener;

import java.io.IOException;

/**
 * Created by aaa
 */
public class CacheWathClientTest extends UsualClientTest {
    @Override
    protected IClient createClient(ClientFactory creator) throws IOException, InterruptedException {
        Listener listener = TestSupport.buildListener();
        return creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newCacheClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).watch(listener).start();
    }
}
