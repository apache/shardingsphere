package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.zookeeper.section.Listener;
import org.apache.zookeeper.ZooDefs;

import java.io.IOException;

/**
 * Created by aaa
 */
public class CacheWathClientTest extends UsualClientTest {
    @Override
    protected IClient createClient(ClientFactory creator) throws IOException, InterruptedException {
        Listener listener = TestSupport.buildListener();
        return creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL).newCacheClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).watch(listener).start();
    }
}
