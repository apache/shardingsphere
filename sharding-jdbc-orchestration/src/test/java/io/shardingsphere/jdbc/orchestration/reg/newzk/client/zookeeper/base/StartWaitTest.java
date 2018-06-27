/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.base;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IClient;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.ClientContext;
import io.shardingsphere.jdbc.orchestration.util.EmbedTestingServer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/*
 * Created by aaa
 */
public class StartWaitTest {
    @Before
    public void start() throws IOException, InterruptedException {
        EmbedTestingServer.start();
    }
    
    @Test
    public void testStart() throws IOException, InterruptedException {
        IClient testClient = new TestClient(new ClientContext(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT));
        boolean result = testClient.start(10000, TimeUnit.MILLISECONDS);
        assert result;
        System.out.println(result);
        testClient.close();
    }
    
    @Test
    public void testSortStart() throws IOException, InterruptedException {
        TestClient testClient = new TestClient(new ClientContext(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT));
        boolean result = testClient.start(100, TimeUnit.MILLISECONDS);
        assert !result;
        System.out.println(result);
        testClient.close();
    }
    
    @Test
    public void testNotStart() throws IOException, InterruptedException {
        TestClient c = new TestClient(new ClientContext(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT));
        boolean r = c.start(100, TimeUnit.MILLISECONDS);
        System.out.println("result : " + r);
        assert !r;
        c.close();
        assert !c.getZookeeper().getState().isConnected();
        
        System.out.println("===============================================================================");
    
        TestClient c1 = new TestClient(new ClientContext(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT));
        boolean r1 = c1.start(10000, TimeUnit.MILLISECONDS);
        System.out.println("result : " + r1);
        assert r1;
        c1.close();
    }
}
