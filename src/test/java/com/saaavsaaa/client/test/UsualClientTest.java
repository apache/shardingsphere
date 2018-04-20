package com.saaavsaaa.client.test;

import com.saaavsaaa.client.zookeeper.ClientFactory;
import com.saaavsaaa.client.zookeeper.UsualClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.List;

/**
 * Created by aaa on 18-4-18.
 */
public class UsualClientTest {
    private static final String SERVERS = "192.168.2.44:2181";
    private static final int SESSION_TIMEOUT = 200000;//ms
    private static final String ROOT = "test";
    private static final String AUTH = "digest";
    
    private UsualClient client = null;
    
    @Before
    public void start() throws IOException, InterruptedException {
        ClientFactory creator = new ClientFactory();
        client = creator.setNamespace(ROOT).authorization(AUTH, AUTH.getBytes()).newClient(SERVERS, SESSION_TIMEOUT).start();
    }
    
    @After
    public void stop() throws InterruptedException {
        client.close();
    }
    
    @Test
    public void createRoot() throws KeeperException, InterruptedException {
        client.createRootNode();
        assert client.getZooKeeper().exists("/" + ROOT, false) != null;
        client.deleteRoot();
        assert client.getZooKeeper().exists("/" + ROOT, false) == null;
    }
    
    @Test
    public void createChild() throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        client.createAllNeedPath(key, "bbb11", CreateMode.PERSISTENT);
        assert client.getZooKeeper().exists("/" + ROOT + "/" + key, false) != null;
        client.deleteCurrentBranch(key);
        assert client.getZooKeeper().exists("/" + ROOT + "/" + key, false) == null;
    }
    
    @Test
    public void isExisted() throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        client.createAllNeedPath(key, "", CreateMode.PERSISTENT);
        assert isExisted(key);
        client.deleteCurrentBranch(key);
    }
    
    @Test
    public void get() throws KeeperException, InterruptedException {
        client.createAllNeedPath("a/b", "bbb11", CreateMode.PERSISTENT);
        String key = "a";
        // TODO: cache
        assert getDirectly(key).equals("");
        key = "a/b";
        assert getDirectly(key).equals("bbb11");
        client.deleteCurrentBranch("a/b");
    }
    
    private String  getDirectly(String key) throws KeeperException, InterruptedException {
        return new String(client.getData(key));
    }

    private boolean isExisted(String key) throws KeeperException, InterruptedException {
        return client.checkExists(key);
    }
    
    @Test
    public void getChildrenKeys() throws KeeperException, InterruptedException {
        String key = "a/b";
        String current = "a";
        client.createAllNeedPath(key, "", CreateMode.PERSISTENT);
        List<String> result = client.getChildren(current);
        Collections.sort(result, new Comparator<String>() {
            public int compare(final String o1, final String o2) {
                return o2.compareTo(o1);
            }
        });
        assert result.get(0).equals("b");
        client.deleteCurrentBranch(key);
    }
    
    @Test
    public void persist() throws KeeperException, InterruptedException {
        String key = "a";
        String value = "aa";
        String newValue = "aaa";
        if (!isExisted(key)) {
            client.createAllNeedPath(key, value, CreateMode.PERSISTENT);
        } else {
            update(key, value);
        }
    
        assert getDirectly(key).equals(value);
    
        update(key, newValue);
        assert getDirectly(key).equals(newValue);
        client.deleteCurrentBranch(key);
    }
    
    private void update(String key, String value) throws KeeperException, InterruptedException {
        client.update(key, value);
    }
    
    @Ignore
    @Test
    public void persistEphemeral() throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        String value = "b1b";
        client.createAllNeedPath(key, value, CreateMode.PERSISTENT);
        if (isExisted(key)) {
            client.deleteAllChild(key);
        }
        client.createAllNeedPath(key, value, CreateMode.EPHEMERAL);
    }
    
    @Test
    public void watch() {
        String key = "";
        EventListener eventListener;
    }
    
    @Test
    public void close() throws Exception {
        client.close();
        assert client.getZooKeeper().getState() == ZooKeeper.States.CLOSED;
    }
}
