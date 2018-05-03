package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.utility.section.Listener;
import com.saaavsaaa.client.utility.PathUtil;
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
 * Created by aaa
 */
public abstract class BaseClientTest {
    protected Client testClient = null;
    
    @Before
    public void start() throws IOException, InterruptedException {
        ClientFactory creator = new ClientFactory();
        testClient = createClient(creator);
    }
    
    protected abstract Client createClient(ClientFactory creator) throws IOException, InterruptedException;
    
    @After
    public void stop() throws InterruptedException {
        testClient.close();
        testClient = null;
    }
    
    @Ignore
    @Test
    public void deleteRoot() throws KeeperException, InterruptedException {
        testClient.deleteNamespace();
        assert testClient.getZooKeeper().exists(Constants.PATH_SEPARATOR + TestSupport.ROOT, false) == null;
    }
    
    protected void createRoot(Client client) throws KeeperException, InterruptedException {
        client.createNamespace();
        assert client.getZooKeeper().exists(Constants.PATH_SEPARATOR + TestSupport.ROOT, false) != null;
        client.deleteNamespace();
        assert client.getZooKeeper().exists(Constants.PATH_SEPARATOR + TestSupport.ROOT, false) == null;
    }
    
    protected void createChild(Client client) throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        client.createAllNeedPath(key, "bbb11", CreateMode.PERSISTENT);
        assert client.getZooKeeper().exists(PathUtil.getRealPath(TestSupport.ROOT, key)/*"/" + ROOT + "/" + key*/, false) != null;
        client.deleteCurrentBranch(key);
        assert client.getZooKeeper().exists(PathUtil.getRealPath(TestSupport.ROOT, key)/*"/" + ROOT + "/" + key*/, false) == null;
    }
    
    protected void deleteBranch(Client client) throws KeeperException, InterruptedException {
        String keyB = "a/b/bb";
        client.createAllNeedPath(keyB, "bbb11", CreateMode.PERSISTENT);
        assert client.getZooKeeper().exists(PathUtil.getRealPath(TestSupport.ROOT, keyB), false) != null;
        String keyC  = "a/c/cc";
        client.createAllNeedPath(keyC, "ccc11", CreateMode.PERSISTENT);
        assert client.getZooKeeper().exists(PathUtil.getRealPath(TestSupport.ROOT, keyC), false) != null;
        client.deleteCurrentBranch(keyC);
        assert client.getZooKeeper().exists(PathUtil.getRealPath(TestSupport.ROOT, keyC), false) == null;
        assert client.getZooKeeper().exists(PathUtil.getRealPath(TestSupport.ROOT, "a"), false) != null;
        client.deleteCurrentBranch(keyB);
        assert client.getZooKeeper().exists(PathUtil.checkPath(TestSupport.ROOT), false) == null;
        client.createAllNeedPath(keyB, "bbb11", CreateMode.PERSISTENT);
        assert client.getZooKeeper().exists(PathUtil.getRealPath(TestSupport.ROOT, keyB), false) != null;
        client.deleteCurrentBranch(keyB);
        assert client.getZooKeeper().exists(PathUtil.checkPath(TestSupport.ROOT), false) == null;
    }
    
    protected void isExisted(Client client) throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        client.createAllNeedPath(key, "", CreateMode.PERSISTENT);
        assert isExisted(key, client);
        client.deleteCurrentBranch(key);
    }
    
    protected void get(Client client) throws KeeperException, InterruptedException {
        client.createAllNeedPath("a/b", "bbb11", CreateMode.PERSISTENT);
        String key = "a";
        // TODO: cache
        assert getDirectly(key, client).equals("");
        key = "a/b";
        assert getDirectly(key, client).equals("bbb11");
        client.deleteCurrentBranch("a/b");
    }
    
    protected void asynGet(Client client) throws KeeperException, InterruptedException {
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
    
    private String getDirectly(String key, Client client) throws KeeperException, InterruptedException {
        return new String(client.getData(key));
    }
    
    private boolean isExisted(String key, Client client) throws KeeperException, InterruptedException {
        return client.checkExists(key);
    }
    
    protected void getChildrenKeys(Client client) throws KeeperException, InterruptedException {
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
    
    protected void persist(Client client) throws KeeperException, InterruptedException {
        String key = "a";
        String value = "aa";
        String newValue = "aaa";
        if (!isExisted(key, client)) {
            client.createAllNeedPath(key, value, CreateMode.PERSISTENT);
        } else {
            updateWithCheck(key, value, client);
        }
        
        assert getDirectly(key, client).equals(value);
    
        updateWithCheck(key, newValue, client);
        assert getDirectly(key, client).equals(newValue);
        client.deleteCurrentBranch(key);
    }
    
    private void updateWithCheck(String key, String value, Client client) throws KeeperException, InterruptedException {
        client.transaction().check(key, Constants.VERSION).setData(key, value.getBytes(Constants.UTF_8), Constants.VERSION).commit();
    }
    
    protected void persistEphemeral(Client client) throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        String value = "b1b";
        client.createAllNeedPath(key, value, CreateMode.PERSISTENT);
//        assert client.getZooKeeper().exists(PathUtil.getRealPath(ROOT, key), null).getEphemeralOwner() == 0;
        Stat stat = new Stat();
        client.getZooKeeper().getData(PathUtil.getRealPath(TestSupport.ROOT, key), false, stat);
        assert  stat.getEphemeralOwner() == 0;
        
        client.deleteAllChildren(key);
        assert !isExisted(key, client);
        client.createAllNeedPath(key, value, CreateMode.EPHEMERAL);
        
        assert client.getZooKeeper().exists(PathUtil.getRealPath(TestSupport.ROOT, key), null).getEphemeralOwner() != 0; // Ephemeral node connection session id
        client.deleteCurrentBranch(key);
    }
    
    protected void delAllChildren(Client client) throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        client.createAllNeedPath(key, "bb", CreateMode.PERSISTENT);
        key = "a/c/cc";
        client.createAllNeedPath(key, "cc", CreateMode.PERSISTENT);
        System.out.println(client.getZooKeeper().exists(PathUtil.getRealPath(TestSupport.ROOT, "a"), null).getNumChildren()); // nearest children count
        assert client.getZooKeeper().exists(PathUtil.getRealPath(TestSupport.ROOT, key), false) != null;
        client.deleteAllChildren("a");
        assert client.getZooKeeper().exists(PathUtil.getRealPath(TestSupport.ROOT, key), false) == null;
        assert client.getZooKeeper().exists("/" + TestSupport.ROOT, false) != null;
        client.deleteNamespace();
    }
    
    protected void watch(Client client) throws KeeperException, InterruptedException {
        List<String> expected = new ArrayList<>();
        expected.add("update_/test/a_value");
        expected.add("update_/test/a_value1");
        expected.add("update_/test/a_value2");
        expected.add("delete_/test/a_");
        List<String> actual = new ArrayList<>();
        
        Listener listener = buildListener(client, actual);
        
        String key = "a";
        Watcher watcher = client.registerWatch(key, listener);
        client.createCurrentOnly(key, "aaa", CreateMode.EPHEMERAL);
        client.checkExists(key, watcher);
        client.update(key, "value");
        System.out.println(new String(client.getData(key)));
        assert client.getDataString(key).equals("value");
        client.update(key, "value1");
        assert client.getDataString(key).equals("value1");
        client.update(key, "value2");
        assert client.getDataString(key).equals("value2");
        client.deleteCurrentBranch(key);
        Thread.sleep(100);
        assert expected.size() == actual.size();
        assert expected.containsAll(actual);
        client.unregisterWatch(key);
    }
    
    protected Listener buildListener(Client client, List<String> actual){
        Listener listener = new Listener() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                System.out.println(event.getPath());
                System.out.println(event.getType());
                
                switch (event.getType()) {
                    case NodeDataChanged:
                    case NodeChildrenChanged: {
                        String result;
                        try {
                            result = new String(client.getZooKeeper().getData(event.getPath(),false, null));
                            System.out.println();
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
                System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            }
        };
        return listener;
    }
    
    protected void close(Client client) throws Exception {
        client.close();
        assert client.getZooKeeper().getState() == ZooKeeper.States.CLOSED;
    }
}
