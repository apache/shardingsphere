package com.saaavsaaa.client.zookeeper;

import java.io.IOException;

/**
 * Created by aaa
 * todo test check cache content
 */
public class CacheClientTest extends UsualClientTest {
    @Override
    protected Client createClient(ClientFactory creator) throws IOException, InterruptedException {
        return creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newCacheClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).start();
    }
}
