package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.action.IClient;
import org.apache.zookeeper.ZooDefs;

import java.io.IOException;

/**
 * Created by aaa
 * todo test check cache content
 */
public class CacheClientTest extends UsualClientTest {
    @Override
    protected IClient createClient(ClientFactory creator) throws IOException, InterruptedException {
        return creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL).newCacheClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).start();
    }
}
