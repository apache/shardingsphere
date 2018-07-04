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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.cache;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IClient;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.ClientFactory;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.base.BaseTest;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.base.TestSupport;
import io.shardingsphere.jdbc.orchestration.util.EmbedTestingServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class PathTreeTest extends BaseTest {
    private PathTree pathTree;
    private IClient testClient;
    
    @Before
    public void start() throws IOException, InterruptedException {
        EmbedTestingServer.start();
        ClientFactory creator = new ClientFactory();
        testClient = creator.setClientNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL)
                .newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).start();
    
        pathTree = new PathTree(TestSupport.ROOT, testClient);
    }
    
    @After
    public void stop() throws InterruptedException {
        pathTree.close();
        testClient.close();
    }
    
    @Test
    public void assertLoad() throws KeeperException, InterruptedException {
        final String keyB = "a/b/bb";
        final String valueB = "bbb11";
        testClient.createAllNeedPath(keyB, valueB, CreateMode.PERSISTENT);
        assert testClient.checkExists(keyB);
        final String keyC  = "a/c/cc";
        final String valueC = "ccc11";
        testClient.createAllNeedPath(keyC, valueC, CreateMode.PERSISTENT);
        assert testClient.checkExists(keyC);
        
        try {
            pathTree.load();
    
            assert valueB.equals(new String(pathTree.getValue(keyB)));
            assert valueC.equals(new String(pathTree.getValue(keyC)));
        } finally {
            testClient.deleteCurrentBranch(keyC);
            testClient.deleteCurrentBranch(keyB);
        }
    }
    
    @Test
    public void assertWatch() throws KeeperException, InterruptedException {
        final String keyB = "a/b/bb";
        final String valueB = "bbb11";
        try {
            pathTree.watch();
    
            if (!testClient.checkExists(keyB)) {
                testClient.createAllNeedPath(keyB, valueB, CreateMode.PERSISTENT);
            }
            
            Thread.sleep(1000);
            assert valueB.equals(new String(pathTree.getValue(keyB)));
        } finally {
            testClient.deleteCurrentBranch(keyB);
        }
    }
}
