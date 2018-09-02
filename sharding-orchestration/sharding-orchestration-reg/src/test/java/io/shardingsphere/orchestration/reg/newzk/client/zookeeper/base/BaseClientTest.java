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

package io.shardingsphere.orchestration.reg.newzk.client.zookeeper.base;

import io.shardingsphere.orchestration.reg.newzk.client.action.IClient;
import io.shardingsphere.orchestration.reg.newzk.client.util.EmbedTestingServer;
import io.shardingsphere.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.ClientFactory;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section.ZookeeperEventListener;
import lombok.Getter;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Getter
public abstract class BaseClientTest extends BaseTest {
    
    private IClient testClient;
    
    @Before
    public void start() throws IOException, InterruptedException {
        EmbedTestingServer.start();
        ClientFactory creator = new ClientFactory();
        testClient = createClient(creator);
        getZooKeeper(testClient);
    }
    
    protected final ZooKeeper getZooKeeper(final IClient client) {
        return ((BaseClient) client).getHolder().getZooKeeper();
    }
    
    protected abstract IClient createClient(ClientFactory creator) throws IOException, InterruptedException;
    
    @After
    public void stop() {
        testClient.close();
        testClient = null;
    }
    
    @Test
    public void assertDeleteRoot() throws KeeperException, InterruptedException {
        ((BaseClient) testClient).createNamespace();
        deleteRoot(testClient);
        assertNull(getZooKeeper(testClient).exists(ZookeeperConstants.PATH_SEPARATOR + TestSupport.ROOT, false));
    }
    
    protected final void createRoot(final IClient client) throws KeeperException, InterruptedException {
        ((BaseClient) client).createNamespace();
        assertNotNull(getZooKeeper(client).exists(ZookeeperConstants.PATH_SEPARATOR + TestSupport.ROOT, false));
        ((BaseClient) client).deleteNamespace();
        assertNull(getZooKeeper(client).exists(ZookeeperConstants.PATH_SEPARATOR + TestSupport.ROOT, false));
    }
    
    protected final void createChild(final IClient client) throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        client.createAllNeedPath(key, "bbb11", CreateMode.PERSISTENT);
        assertNotNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, key), false));
        client.deleteCurrentBranch(key);
        assertNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, key), false));
    }
    
    protected final void deleteBranch(final IClient client) throws KeeperException, InterruptedException {
        String keyB = "a/b/bb";
        String valueB = "bbb11";
        client.createAllNeedPath(keyB, valueB, CreateMode.PERSISTENT);
        assertNotNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, keyB), false));
        String keyC = "a/c/cc";
        client.createAllNeedPath(keyC, "ccc11", CreateMode.PERSISTENT);
        assertNotNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, keyC), false));
        client.deleteCurrentBranch(keyC);
        assertNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, keyC), false));
        assertNotNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, "a"), false));
        client.deleteCurrentBranch(keyB);
        assertNull(getZooKeeper(client).exists(PathUtil.checkPath(TestSupport.ROOT), false));
        client.createAllNeedPath(keyB, valueB, CreateMode.PERSISTENT);
        assertNotNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, keyB), false));
        assertThat(client.getDataString(keyB), is(valueB));
        client.deleteCurrentBranch(keyB);
        assertNull(getZooKeeper(client).exists(PathUtil.checkPath(TestSupport.ROOT), false));
    }
    
    protected final void isExisted(final IClient client) throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        client.createAllNeedPath(key, "", CreateMode.PERSISTENT);
        assertTrue(isExisted(key, client));
        client.deleteCurrentBranch(key);
    }
    
    private boolean isExisted(final String key, final IClient client) throws KeeperException, InterruptedException {
        return client.checkExists(key);
    }
    
    protected final void get(final IClient client) throws KeeperException, InterruptedException {
        String value = "bbb11";
        client.createAllNeedPath("a/b", value, CreateMode.PERSISTENT);
        String key = "a";
        assertThat(getDirectly(key, client), is(""));
        key = "a/b";
        assertThat(getDirectly(key, client), is(value));
        client.deleteCurrentBranch("a/b");
    }
    
    protected final void asyncGet(final IClient client) throws KeeperException, InterruptedException {
        final CountDownLatch ready = new CountDownLatch(1);
        String key = "a/b";
        String value = "bbb11";
        client.createAllNeedPath(key, value, CreateMode.PERSISTENT);
        AsyncCallback.DataCallback callback = new AsyncCallback.DataCallback() {
            @Override
            public void processResult(final int rc, final String path, final Object ctx, final byte[] data, final Stat stat) {
                assertThat(new String(data), is(ctx));
                ready.countDown();
            }
        };
        client.getData(key, callback, value);
        ready.await();
        client.deleteCurrentBranch("a/b");
    }
    
    private String getDirectly(final String key, final IClient client) throws KeeperException, InterruptedException {
        return new String(client.getData(key));
    }
    
    protected final void getChildrenKeys(final IClient client) throws KeeperException, InterruptedException {
        String key = "a/b";
        String current = "a";
        client.createAllNeedPath(key, "", CreateMode.PERSISTENT);
        List<String> result = client.getChildren(current);
        Collections.sort(result, new Comparator<String>() {
            
            public int compare(final String o1, final String o2) {
                return o2.compareTo(o1);
            }
        });
        assertThat(result.get(0), is("b"));
        client.deleteCurrentBranch(key);
    }
    
    protected final void persist(final IClient client) throws KeeperException, InterruptedException {
        String key = "a";
        String value = "aa";
        String newValue = "aaa";
        if (!isExisted(key, client)) {
            client.createAllNeedPath(key, value, CreateMode.PERSISTENT);
        } else {
            updateWithCheck(key, value, client);
        }
    
        assertThat(getDirectly(key, client), is(value));
    
        updateWithCheck(key, newValue, client);
        assertThat(getDirectly(key, client), is(newValue));
        client.deleteCurrentBranch(key);
    }
    
    private void updateWithCheck(final String key, final String value, final IClient client) throws KeeperException, InterruptedException {
        client.update(key, value);
    }
    
    protected final void persistEphemeral(final IClient client) throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        String value = "b1b";
        client.createAllNeedPath(key, value, CreateMode.PERSISTENT);
        Stat stat = new Stat();
        getZooKeeper(client).getData(PathUtil.getRealPath(TestSupport.ROOT, key), false, stat);
        assertThat(stat.getEphemeralOwner(), is(0L));
        
        client.deleteAllChildren(key);
        assertFalse(isExisted(key, client));
        client.createAllNeedPath(key, value, CreateMode.EPHEMERAL);
        
        assertThat(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, key), null).getEphemeralOwner(), is(getZooKeeper(client).getSessionId()));
        client.deleteCurrentBranch(key);
    }
    
    protected final void delAllChildren(final IClient client) throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        client.createAllNeedPath(key, "bb", CreateMode.PERSISTENT);
        key = "a/c/cc";
        client.createAllNeedPath(key, "cc", CreateMode.PERSISTENT);
        assertNotNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, key), false));
        client.deleteAllChildren("a");
        assertNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, key), false));
        assertNotNull(getZooKeeper(client).exists("/" + TestSupport.ROOT, false));
        ((BaseClient) client).deleteNamespace();
    }
    
    protected final void watch(final IClient client) throws KeeperException, InterruptedException {
        List<String> actual = new ArrayList<>();
        final ZookeeperEventListener zookeeperEventListener = buildListener(client, actual);
        
        String key = "a";
        client.registerWatch(key, zookeeperEventListener);
        client.createCurrentOnly(key, "aaa", CreateMode.EPHEMERAL);
        client.checkExists(key, new Watcher() {
            
            @Override
            public void process(final WatchedEvent event) {
                zookeeperEventListener.process(event);
            }
        });
        String value = "value0";
        client.update(key, value);
        assertThat(client.getDataString(key), is(value));
        sleep(200);
        
        String value1 = "value1";
        client.update(key, value1);
        assertThat(client.getDataString(key), is(value1));
        sleep(200);
        
        String value2 = "value2";
        client.update(key, value2);
        assertThat(client.getDataString(key), is(value2));
        sleep(200);
        
        client.deleteCurrentBranch(key);
        sleep(200);

        //The acquisition value is after the reception of the event,
        //so the value may be not equal.
        assertThat(actual, hasItems("update_/test/a_value0", "update_/test/a_value1", "update_/test/a_value2", "delete_/test/a_"));
        client.unregisterWatch(zookeeperEventListener.getKey());
    }
    
    protected final void watchRegister(final IClient client) throws KeeperException, InterruptedException {
        List<String> actual = new ArrayList<>();
        
        final ZookeeperEventListener zookeeperEventListener = buildListener(client, actual);
        
        String key = "a";
        client.registerWatch(key, zookeeperEventListener);
        client.createCurrentOnly(key, "aaa", CreateMode.EPHEMERAL);
        sleep(100);

        String value = "value0";
        client.update(key, value);
        sleep(100);
        
        String value1 = "value1";
        client.update(key, value1);
        sleep(100);
        
        String value2 = "value2";
        client.update(key, value2);
        sleep(100);
        
        client.deleteCurrentBranch(key);
        sleep(100);
        
        //The acquisition value is after the reception of the event,
        //so the value may be not equal.
        assertThat(actual, hasItems("update_/test/a_value0", "update_/test/a_value1", "update_/test/a_value2", "delete_/test/a_"));
        client.unregisterWatch(zookeeperEventListener.getKey());
    }
    
    private ZookeeperEventListener buildListener(final IClient client, final List<String> actual) {
        return new ZookeeperEventListener(null) {
            
            @Override
            public void process(final WatchedEvent event) {
                switch (event.getType()) {
                    case NodeDataChanged:
                    case NodeChildrenChanged:
                        try {
                            actual.add("update_" + event.getPath() + "_" + client.getDataString(event.getPath()));
                        } catch (final KeeperException | InterruptedException ignored) {
                        }
                        break;
                    case NodeDeleted:
                        actual.add("delete_" + event.getPath() + "_");
                        break;
                    default:
                }
            }
        };
    }
    
    protected final void close(final IClient client) {
        client.close();
        assertThat(getZooKeeper(client).getState(), is(ZooKeeper.States.CLOSED));
    }
}
