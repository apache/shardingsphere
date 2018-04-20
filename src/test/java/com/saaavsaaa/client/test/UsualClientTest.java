package com.saaavsaaa.client.test;

import com.saaavsaaa.client.zookeeper.ClientFactory;
import com.saaavsaaa.client.zookeeper.ZookeeperClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.junit.BeforeClass;
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
    static final String SERVERS = "192.168.2.44:2181";
    static final int SESSION_TIMEOUT = 200000;//ms
    
    static ZookeeperClient client = null;
    
//    @BeforeClass
    public static void start() throws IOException, InterruptedException {
        ClientFactory creator = new ClientFactory();
        client = creator.setNamespace("test").authorization("digest", "digest".getBytes()).newClient(SERVERS, SESSION_TIMEOUT).start();
    }
    
//    @Test
    public void createRoot() throws KeeperException, InterruptedException {
        client.createRootNode();
    }
    
//    @Test
    public void createChild() throws KeeperException, InterruptedException {
        client.createCurrentOnly("a/b/bb", "bbb11".getBytes(), CreateMode.PERSISTENT);
    }
    
//    @Test
    public void get() throws KeeperException, InterruptedException {
        String key = "a/b";
        /*TreeCache cache = findTreeCache(key);
        if (null == cache) {
            return getDirectly(key);
        }
        ChildData resultInCache = cache.getCurrentData(key);
        if (null != resultInCache) {
            return null == resultInCache.getData() ? null : new String(resultInCache.getData(), Charsets.UTF_8);
        }*/
        getDirectly(key);
    }
    
    private void getDirectly(String key) throws KeeperException, InterruptedException {
        System.out.println(new String(client.getData(key)));
    }
    
//    @Test
    public void getDirectly() throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        getDirectly(key);
    }
    
//    @Test
    public void isExisted() throws KeeperException, InterruptedException {
        String key = "a";
        System.out.println(isExisted(key));
    }

    private boolean isExisted(String key) throws KeeperException, InterruptedException {
        return client.checkExists(key);
    }
    
//    @Test
    public void getChildrenKeys() throws KeeperException, InterruptedException {
        String key = "a";
        List<String> result = client.getChildren(key);
        Collections.sort(result, new Comparator<String>() {
            public int compare(final String o1, final String o2) {
                return o2.compareTo(o1);
            }
        });
        System.out.println(result);
    }
    
//    @Test
    public void persist() throws KeeperException, InterruptedException {
        String key = "a";
        String value = "aa";
        if (!isExisted(key)) {
            client.createAllNeedPath(key, value.getBytes(), CreateMode.PERSISTENT);
        } else {
            update(key, value);
        }
    }
    
    private void update(String key, String value) throws KeeperException, InterruptedException {
        client.update(key, value.getBytes());
    }
    
//    @Test
    public void update() throws KeeperException, InterruptedException {
        String key = "a";
        String value = "aaa";
        update(key, value); //inTransaction().check().forPath(key).and().setData().forPath().and().commit();
        getDirectly(key);
        value = "aa";
        client.updateInTransaction(key, value.getBytes());
        getDirectly(key);
    }
    
//    @Test
    public void persistEphemeral() throws KeeperException, InterruptedException {
        String key = "bb";
        String value = "b1b";
        if (isExisted(key)) {
            client.deleteCurrentBranch(key);
        }
        client.createAllNeedPath(key, value.getBytes(), CreateMode.EPHEMERAL);
    }
    
//    @Test
    public void watch() {
        String key = "a";
        final String path = key + "/";
        EventListener eventListener;
    }
    
//    @Test
    public void close() throws Exception {
        
    }
}
