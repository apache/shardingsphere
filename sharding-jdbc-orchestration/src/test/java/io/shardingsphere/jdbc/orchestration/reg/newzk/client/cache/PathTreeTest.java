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
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.ZookeeperEventListener;
import io.shardingsphere.jdbc.orchestration.util.EmbedTestingServer;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Slf4j
public class PathTreeTest extends BaseTest {
    
    private PathTree pathTree;
    
    private IClient testClient;
    
    private ZookeeperEventListener zookeeperEventListener = new ZookeeperEventListener() {
        
        @Override
        public void process(final WatchedEvent event) {
            log.debug("debug event :" + event.toString());
        }
    };
    
    @Before
    public void start() throws IOException, InterruptedException {
        EmbedTestingServer.start();
        ClientFactory creator = new ClientFactory();
        testClient = creator.setClientNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL)
                .watch(zookeeperEventListener).newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).start();
    
        pathTree = new PathTree(TestSupport.ROOT, testClient);
    }
    
    @After
    public void stop() {
        pathTree.close();
        testClient.close();
    }
    
    @Test
    public void assertLoad() throws KeeperException, InterruptedException {
        final String keyB = "a/b/bb";
        final String valueB = "bbb11";
        testClient.createAllNeedPath(keyB, valueB, CreateMode.PERSISTENT);
        assertTrue(testClient.checkExists(keyB));
        final String keyC = "a/c/cc";
        final String valueC = "ccc11";
        testClient.createAllNeedPath(keyC, valueC, CreateMode.PERSISTENT);
        assertTrue(testClient.checkExists(keyC));
        
        try {
            pathTree.load();
    
            assertThat(new String(pathTree.getValue(keyB)), is(valueB));
            assertThat(new String(pathTree.getValue(keyC)), is(valueC));
        } finally {
            testClient.deleteCurrentBranch(keyC);
            testClient.deleteCurrentBranch(keyB);
        }
    }
    
    @Ignore
    @Test
    public void assertWatch() throws KeeperException, InterruptedException {
        final String keyB = "a/b/bb";
        final String valueB = "bbb11";
        try {
            createRootOnly(testClient);
            pathTree.watch();
    
            testClient.createAllNeedPath(keyB, valueB, CreateMode.PERSISTENT);
            testClient.update(keyB, "111");
            
            Thread.sleep(1000);
            assertThat(new String(pathTree.getValue(keyB)), is(valueB));
        } finally {
            testClient.deleteCurrentBranch(keyB);
        }
    }
}
