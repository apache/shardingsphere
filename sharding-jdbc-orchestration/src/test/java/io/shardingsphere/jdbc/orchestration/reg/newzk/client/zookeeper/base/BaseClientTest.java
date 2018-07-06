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
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.ClientFactory;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.Listener;
import io.shardingsphere.jdbc.orchestration.util.EmbedTestingServer;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public abstract class BaseClientTest extends BaseTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseClient.class);
    
    protected IClient testClient = null;
    
    @Before
    public void start() throws IOException, InterruptedException {
        EmbedTestingServer.start();
        ClientFactory creator = new ClientFactory();
        testClient = createClient(creator);
        getZooKeeper(testClient);
    }
    
    private ZooKeeper getZooKeeper(IClient client){
        return ((BaseClient) client).getHolder().getZooKeeper();
    }
    
    protected abstract IClient createClient(ClientFactory creator) throws IOException, InterruptedException;
    
    @After
    public void stop() throws InterruptedException {
        testClient.close();
        testClient = null;
    }
    
    @Test
    public void assertDeleteRoot() throws KeeperException, InterruptedException {
        ((BaseClient)testClient).createNamespace();
        deleteRoot(testClient);
        Assert.assertNull(getZooKeeper(testClient).exists(ZookeeperConstants.PATH_SEPARATOR + TestSupport.ROOT, false));
    }
    
    protected void createRoot(IClient client) throws KeeperException, InterruptedException {
        ((BaseClient)client).createNamespace();
        Assert.assertNotNull(getZooKeeper(client).exists(ZookeeperConstants.PATH_SEPARATOR + TestSupport.ROOT, false));
        ((BaseClient)client).deleteNamespace();
        Assert.assertNull(getZooKeeper(client).exists(ZookeeperConstants.PATH_SEPARATOR + TestSupport.ROOT, false));
    }
    
    protected void createChild(IClient client) throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        client.createAllNeedPath(key, "bbb11", CreateMode.PERSISTENT);
        Assert.assertNotNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, key), false));
        client.deleteCurrentBranch(key);
        Assert.assertNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, key), false));
    }
    
    protected void deleteBranch(IClient client) throws KeeperException, InterruptedException {
        String keyB = "a/b/bb";
        String valueB = "bbb11";
        client.createAllNeedPath(keyB, valueB, CreateMode.PERSISTENT);
        Assert.assertNotNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, keyB), false));
        String keyC  = "a/c/cc";
        client.createAllNeedPath(keyC, "ccc11", CreateMode.PERSISTENT);
        Assert.assertNotNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, keyC), false));
        client.deleteCurrentBranch(keyC);
        Assert.assertNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, keyC), false));
        Assert.assertNotNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, "a"), false));
        client.deleteCurrentBranch(keyB);
        Assert.assertNull(getZooKeeper(client).exists(PathUtil.checkPath(TestSupport.ROOT), false));
        client.createAllNeedPath(keyB, valueB, CreateMode.PERSISTENT);
        Assert.assertNotNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, keyB), false));
        Assert.assertEquals(valueB, client.getDataString(keyB));
        client.deleteCurrentBranch(keyB);
        Assert.assertNull(getZooKeeper(client).exists(PathUtil.checkPath(TestSupport.ROOT), false));
    }
    
    protected void isExisted(IClient client) throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        client.createAllNeedPath(key, "", CreateMode.PERSISTENT);
        Assert.assertTrue(isExisted(key, client));
        client.deleteCurrentBranch(key);
    }
    
    protected void get(IClient client) throws KeeperException, InterruptedException {
        String value = "bbb11";
        client.createAllNeedPath("a/b", value, CreateMode.PERSISTENT);
        String key = "a";
        Assert.assertEquals(getDirectly(key, client), "");
        key = "a/b";
        Assert.assertEquals(getDirectly(key, client), value);
        client.deleteCurrentBranch("a/b");
    }
    
    protected void asynGet(IClient client) throws KeeperException, InterruptedException {
        final CountDownLatch ready = new CountDownLatch(1);
        String key = "a/b";
        String value = "bbb11";
        client.createAllNeedPath(key, value, CreateMode.PERSISTENT);
        AsyncCallback.DataCallback callback = new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                String result = new String(data);
                LOGGER.info(new StringBuffer().append("rc:").append(rc).append(",path:").append(path).append(",ctx:").append(ctx).append(",stat:").append(stat).toString());
                Assert.assertEquals(result, ctx);
                ready.countDown();
            }
        };
        client.getData(key, callback, value);
        ready.await();
        client.deleteCurrentBranch("a/b");
    }
    
    private String getDirectly(String key, IClient client) throws KeeperException, InterruptedException {
        return new String(client.getData(key));
    }
    
    private boolean isExisted(String key, IClient client) throws KeeperException, InterruptedException {
        return client.checkExists(key);
    }
    
    protected void getChildrenKeys(IClient client) throws KeeperException, InterruptedException {
        String key = "a/b";
        String current = "a";
        client.createAllNeedPath(key, "", CreateMode.PERSISTENT);
        List<String> result = client.getChildren(current);
        Collections.sort(result, new Comparator<String>() {
            public int compare(final String o1, final String o2) {
                return o2.compareTo(o1);
            }
        });
        Assert.assertEquals(result.get(0), "b");
        client.deleteCurrentBranch(key);
    }
    
    protected void persist(IClient client) throws KeeperException, InterruptedException {
        String key = "a";
        String value = "aa";
        String newValue = "aaa";
        if (!isExisted(key, client)) {
            client.createAllNeedPath(key, value, CreateMode.PERSISTENT);
        } else {
            updateWithCheck(key, value, client);
        }
    
        Assert.assertEquals(getDirectly(key, client), value);
    
        updateWithCheck(key, newValue, client);
        Assert.assertEquals(getDirectly(key, client), newValue);
        client.deleteCurrentBranch(key);
    }
    
    private void updateWithCheck(String key, String value, IClient client) throws KeeperException, InterruptedException {
        client.update(key, value);
    }
    
    protected void persistEphemeral(IClient client) throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        String value = "b1b";
        client.createAllNeedPath(key, value, CreateMode.PERSISTENT);
        Stat stat = new Stat();
        getZooKeeper(client).getData(PathUtil.getRealPath(TestSupport.ROOT, key), false, stat);
        Assert.assertThat(stat.getEphemeralOwner(), CoreMatchers.is(0L));
        
        client.deleteAllChildren(key);
        Assert.assertFalse(isExisted(key, client));
        client.createAllNeedPath(key, value, CreateMode.EPHEMERAL);
        
        Assert.assertNotEquals(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, key), null).getEphemeralOwner(), 0L);
        client.deleteCurrentBranch(key);
    }
    
    protected void delAllChildren(IClient client) throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        client.createAllNeedPath(key, "bb", CreateMode.PERSISTENT);
        key = "a/c/cc";
        client.createAllNeedPath(key, "cc", CreateMode.PERSISTENT);
        LOGGER.debug("getNumChildren:" + getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, "a"), null).getNumChildren());
        Assert.assertNotNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, key), false));
        client.deleteAllChildren("a");
        Assert.assertNull(getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, key), false));
        Assert.assertNotNull(getZooKeeper(client).exists("/" + TestSupport.ROOT, false));
        ((BaseClient)client).deleteNamespace();
    }
    
    protected void watch(IClient client) throws KeeperException, InterruptedException {
        List<String> expected = new ArrayList<>();
        expected.add("update_/test/a_value");
        expected.add("update_/test/a_value1");
        expected.add("update_/test/a_value2");
        expected.add("delete_/test/a_");
        List<String> actual = new ArrayList<>();
        
        final Listener listener = buildListener(client, actual);
        
        String key = "a";
        client.registerWatch(key, listener);
        client.createCurrentOnly(key, "aaa", CreateMode.EPHEMERAL);
        client.checkExists(key, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                listener.process(event);
            }
        });
        client.update(key, "value");
        LOGGER.info(new String(client.getData(key)));
        Assert.assertEquals(client.getDataString(key), "value");
        client.update(key, "value1");
        Assert.assertEquals(client.getDataString(key), "value1");
        client.update(key, "value2");
        Assert.assertEquals(client.getDataString(key), "value2");
        client.deleteCurrentBranch(key);
        sleep(1000);
        Assert.assertEquals(expected.size(), actual.size());

        //The acquisition value is after the reception of the event,
        //so the value may be not equal.
        Assert.assertTrue(expected.containsAll(actual));
        client.unregisterWatch(listener.getKey());
    }
    
    protected Listener buildListener(final IClient client, final List<String> actual){
        return new Listener(null) {
            @Override
            public void process(WatchedEvent event) {
                LOGGER.info(event.getPath());
                LOGGER.info(event.getType().name());
                
                switch (event.getType()) {
                    case NodeDataChanged:
                    case NodeChildrenChanged: {
                        String result;
                        try {
                            result = new String(getZooKeeper(client).getData(event.getPath(),false, null));
                            LOGGER.info(result);
                        } catch (KeeperException e) {
                            result = e.getMessage();
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            result = e.getMessage();
                            e.printStackTrace();
                        }
                        actual.add(new StringBuilder().append("update_").append(event.getPath()).append("_").append(result).toString());
                        break;
                    }
                    case NodeDeleted: {
                        actual.add(new StringBuilder().append("delete_").append(event.getPath()).append("_").toString());
                        break;
                    }
                    default:
                        actual.add(new StringBuilder().append("ignore_").append(event.getPath()).append("_").append(event.getType()).toString());
                        break;
                }
            }
        };
    }
    
    protected void close(IClient client) throws Exception {
        client.close();
        Assert.assertEquals(getZooKeeper(client).getState(), ZooKeeper.States.CLOSED);
    }
}
