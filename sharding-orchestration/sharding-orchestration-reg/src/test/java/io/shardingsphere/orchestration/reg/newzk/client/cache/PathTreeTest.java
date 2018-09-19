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

package io.shardingsphere.orchestration.reg.newzk.client.cache;

import io.shardingsphere.orchestration.reg.newzk.client.action.IClient;
import io.shardingsphere.orchestration.reg.newzk.client.util.EmbedTestingServer;
import io.shardingsphere.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.ClientFactory;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.base.BaseTest;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.base.TestSupport;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
    
    @Test
    public void assertGetChildren() throws KeeperException, InterruptedException {
        final String keyB = "a/b";
        final String valueB = "bbb11";

        final String keyC = "a/c";
        final String valueC = "ccc11";
        
        try {
            pathTree.watch();
            testClient.createAllNeedPath(keyB, valueB, CreateMode.PERSISTENT);
            Thread.sleep(200);
            testClient.createAllNeedPath(keyC, valueC, CreateMode.PERSISTENT);
            Thread.sleep(200);
    
            assertThat(pathTree.getChildren("a"), hasItems(valueB, valueC));
        } finally {
            testClient.deleteCurrentBranch(keyC);
            testClient.deleteCurrentBranch(keyB);
        }
    }
    
    @Test
    public void assertPut() {
        final String key = "a/b/bb";
        final String value = "bbb11";
        pathTree.put(key, value);
        assertThat(pathTree.getValue("a"), is(ZookeeperConstants.NOTHING_DATA));
        assertThat(pathTree.getValue("a/b"), is(ZookeeperConstants.NOTHING_DATA));
        assertThat(pathTree.getValue(key), is(value.getBytes(ZookeeperConstants.UTF_8)));
    }
    
    @Test
    public void assertGetValue() throws KeeperException, InterruptedException {
        final String key = "a/b/bb";
        final String value = "bbb11";
        try {
            pathTree.watch();
            testClient.createAllNeedPath(key, value, CreateMode.PERSISTENT);
            Thread.sleep(200);
            assertThat(pathTree.getValue(key), is(value.getBytes(ZookeeperConstants.UTF_8)));
        } finally {
            testClient.deleteCurrentBranch(key);
        }
    }
    
    @Test
    public void assertDelete() throws KeeperException, InterruptedException {
        final String key = "a/b/bb";
        final String value = "bbb11";
        try {
            pathTree.watch();
            testClient.createAllNeedPath(key, value, CreateMode.PERSISTENT);
            Thread.sleep(200);
            pathTree.delete(key);
            assertNull(pathTree.getValue(key));
        } finally {
            testClient.deleteCurrentBranch(key);
        }
    }

    @Test
    public void assertWatch() throws KeeperException, InterruptedException {
        final String key = "a/b/bb";
        final String value = "bbb11";
        final String valueNew = "111";
        try {
            createRootOnly(testClient);
            pathTree.watch();

            testClient.createAllNeedPath(key, value, CreateMode.PERSISTENT);
            Thread.sleep(200);
            testClient.update(key, valueNew);
            
            Thread.sleep(1000);
            assertThat(pathTree.getValue(key), is(valueNew.getBytes(ZookeeperConstants.UTF_8)));
        } finally {
            testClient.deleteCurrentBranch(key);
        }
    }
    
    @Test
    public void assertRefreshPeriodic() throws KeeperException, InterruptedException {
        final String key = "a/b/bb";
        final String value = "bbb11";
        final String valueNew = "111";
        try {
            testClient.createAllNeedPath(key, value, CreateMode.PERSISTENT);
            pathTree.refreshPeriodic(100);
            sleep(2000);
            assertThat(pathTree.getValue(key), is(value.getBytes(ZookeeperConstants.UTF_8)));
            testClient.update(key, valueNew);
            sleep(2000);
            assertThat(pathTree.getValue(key), is(valueNew.getBytes(ZookeeperConstants.UTF_8)));
            
            pathTree.refreshPeriodic(10);
        } catch (final IllegalStateException ex) {
            assertThat(ex.getMessage(), is("period already set"));
        } finally {
            pathTree.stopRefresh();
            testClient.deleteCurrentBranch(key);
        }
    }
    
    @Test
    public void assertStopRefresh() {
        try {
            pathTree.refreshPeriodic(1);
            sleep(100);
            pathTree.refreshPeriodic(1);
        } catch (final IllegalStateException ex) {
            assertThat(ex.getMessage(), is("period already set"));
            pathTree.stopRefresh();
            pathTree.refreshPeriodic(1);
        }
    }
}
