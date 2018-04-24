package com.saaavsaaa.client.test;

import com.saaavsaaa.client.untils.Listener;
import com.saaavsaaa.client.untils.PathUtil;
import com.saaavsaaa.client.untils.StringUtil;
import com.saaavsaaa.client.zookeeper.BaseClient;
import com.saaavsaaa.client.zookeeper.ClientFactory;
import com.saaavsaaa.client.zookeeper.UsualClient;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by aaa on 18-4-18.
 */
public class UsualClientTest {
    private static final String SERVERS = "192.168.2.44:2181";
    private static final int SESSION_TIMEOUT = 200000;//ms
    private static final String ROOT = "test";
    private static final String AUTH = "digest";
    
    private UsualClient client = null;
    
    /*@Before
    public void start() throws IOException, InterruptedException {
        ClientFactory creator = new ClientFactory();
        client = creator.setNamespace(ROOT).authorization(AUTH, AUTH.getBytes()).newClient(SERVERS, SESSION_TIMEOUT).start();
    }*/
    
    @Before
    public void startWithWatch() throws IOException, InterruptedException {
        ClientFactory creator = new ClientFactory();
        Listener listener = buildListener();
        client = creator.setNamespace(ROOT).authorization(AUTH, AUTH.getBytes()).newClient(SERVERS, SESSION_TIMEOUT).watch(listener).start();
    }
    
    private Listener buildListener(){
        Listener listener = new Listener() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("==========================================================");
                System.out.println(event.getPath());
                System.out.println(event.getState());
                System.out.println(event.getType());
                System.out.println("==========================================================");
            }
        };
        return listener;
    }
    
    @After
    public void stop() throws InterruptedException {
        client.close();
    }
    
    @Test
    public void createRoot() throws KeeperException, InterruptedException {
        client.createNamespace();
        assert client.getZooKeeper().exists(PathUtil.PATH_SEPARATOR + ROOT, false) != null;
        client.deleteNamespace();
        assert client.getZooKeeper().exists(PathUtil.PATH_SEPARATOR + ROOT, false) == null;
    }
    
    @Test
    public void createChild() throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        client.createAllNeedPath(key, "bbb11", CreateMode.PERSISTENT);
        assert client.getZooKeeper().exists(PathUtil.getRealPath(ROOT, key)/*"/" + ROOT + "/" + key*/, false) != null;
        client.deleteCurrentBranch(key);
        assert client.getZooKeeper().exists(PathUtil.getRealPath(ROOT, key)/*"/" + ROOT + "/" + key*/, false) == null;
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
    
    @Test
    public void asynGet() throws KeeperException, InterruptedException {
        CountDownLatch ready = new CountDownLatch(1);
        String key = "a/b";
        String value = "bbb11";
        client.createAllNeedPath(key, value, CreateMode.PERSISTENT);
        AsyncCallback.DataCallback callback = new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                String result = new String(data);
                System.out.println(new StringBuffer().append("rc:").append(rc).append(",path:").append(path).append(",ctx:").append(ctx).append(",stat:").append(stat));
                assert result.equals(ctx);
                ready.countDown();
            }
        };
        client.getData(key, callback, value);
        ready.await();
        client.deleteCurrentBranch("a/b");
    }
    
    private String getDirectly(String key) throws KeeperException, InterruptedException {
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
        client.updateInTransaction(key, value);
    }

    @Test
    public void persistEphemeral() throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        String value = "b1b";
        client.createAllNeedPath(key, value, CreateMode.PERSISTENT);
//        assert client.getZooKeeper().exists(PathUtil.getRealPath(ROOT, key), null).getEphemeralOwner() == 0;
        Stat stat = new Stat();
        client.getZooKeeper().getData(PathUtil.getRealPath(ROOT, key), false, stat);
        assert  stat.getEphemeralOwner() == 0;
        
        client.deleteAllChild(key);
        assert !isExisted(key);
        client.createAllNeedPath(key, value, CreateMode.EPHEMERAL);
        
        assert client.getZooKeeper().exists(PathUtil.getRealPath(ROOT, key), null).getEphemeralOwner() != 0; // Ephemeral node connection session id
        client.deleteCurrentBranch(key);
    }
    
    @Test
    public void delAllChildren() throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        client.createAllNeedPath(key, "bb", CreateMode.PERSISTENT);
        key = "a/c/cc";
        client.createAllNeedPath(key, "cc", CreateMode.PERSISTENT);
        System.out.println(client.getZooKeeper().exists(PathUtil.getRealPath(ROOT, "a"), null).getNumChildren()); // nearest children count
        assert client.getZooKeeper().exists(PathUtil.getRealPath(ROOT, key), false) != null;
        client.deleteAllChild("a");
        assert client.getZooKeeper().exists(PathUtil.getRealPath(ROOT, key), false) == null;
        assert client.getZooKeeper().exists("/" + ROOT, false) != null;
        client.deleteNamespace();
    }
    
    @Test
    public void watch() throws KeeperException, InterruptedException {
        List<String> expected = new ArrayList<>();
        expected.add("ignore_/test_");
        expected.add("update_/test/a_value");
        expected.add("update_/test/a_value1");
        expected.add("update_/test/a_value2");
        expected.add("delete_/test/a_value2");
        List<String> actual = new ArrayList<>();
        Listener listener = buildListener(actual);
        String key = "a";
        Watcher watcher = client.watch(key, listener);
        client.createNamespace();
        client.createCurrentOnly(key, "aaa", CreateMode.EPHEMERAL);
        client.checkExists(key, watcher);
        client.updateInTransaction(key, "value");
        System.out.println(new String(client.getData(key)));
        assert client.getDataString(key).equals("value");
        client.updateInTransaction(key, "value1");
        assert client.getDataString(key).equals("value1");
        client.updateInTransaction(key, "value2");
        assert client.getDataString(key).equals("value2");
        client.deleteCurrentBranch(key);
        assert expected.size() == actual.size();
        assert expected.containsAll(actual);
    }
    
    private Listener buildListener(List<String> actual){
        EventListener eventListener = new EventListener() {
            @Override
            public void onChange(DataChangedEvent event) {
                System.out.println(event.getKey() + " : " + event.getValue());
            }
        };
        Listener listener = new Listener() {
            @Override
            public void process(WatchedEvent event) {
                byte[] data = new byte[0];
                try {
                    data = client.getZooKeeper().getData(event.getPath(),false, null);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String result = null == data ? null : new String(data, StringUtil.UTF_8);
                eventListener.onChange(new DataChangedEvent(getEventType(event, result), event.getPath(), result));
            }
        
            private DataChangedEvent.Type getEventType(final WatchedEvent event, final String result) {
                switch (event.getType()) {
                    case NodeDataChanged:
                    case NodeChildrenChanged: {
                        actual.add(new StringBuilder().append("update_").append(event.getPath()).append("_").append(result).toString());
                        return DataChangedEvent.Type.UPDATED;
                    }
                    case NodeDeleted: {
                        actual.add(new StringBuilder().append("delete_").append(event.getPath()).append("_").append(result).toString());
                        return DataChangedEvent.Type.DELETED;
                    }
                    default:
                        actual.add(new StringBuilder().append("ignore_").append(event.getPath()).append("_").append(result).toString());
                        return DataChangedEvent.Type.IGNORED;
                }
            }
        };
        return listener;
    }
    
    @Test
    public void close() throws Exception {
        client.close();
        assert client.getZooKeeper().getState() == ZooKeeper.States.CLOSED;
    }
}

interface EventListener {
    void onChange(DataChangedEvent event);
}

class DataChangedEvent {
    
    public Type getEventType() {
        return eventType;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getValue() {
        return value;
    }
    
    public DataChangedEvent(Type eventType, String key, String value) {
        this.eventType = eventType;
        this.key = key;
        this.value = value;
    }
    
    private final Type eventType;
    
    private final String key;
    
    private final String value;
    
    /**
     * Data changed event type.
     */
    public enum Type {
        
        UPDATED, DELETED, IGNORED
    }
}