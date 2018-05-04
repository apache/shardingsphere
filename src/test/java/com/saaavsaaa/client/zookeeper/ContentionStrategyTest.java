package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.utility.section.Listener;
import com.saaavsaaa.client.zookeeper.strategy.StrategyType;

import java.io.IOException;

/**
 * Created by aaa on 18-5-4.
 */
public class ContentionStrategyTest extends UsualClientTest {
    @Override
    protected Client createClient(final ClientFactory creator) throws IOException, InterruptedException {
        Listener listener = TestSupport.buildListener();
        Client client = creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newUsualClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).watch(listener).start();
        client.useStrategy(StrategyType.CONTENTION);
        return client;
    }
    
    //todo test node contention case
}
