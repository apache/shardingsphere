package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.utility.section.Listener;
import com.saaavsaaa.client.zookeeper.base.BaseClient;
import com.saaavsaaa.client.zookeeper.base.BaseClientTest;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaa
 */
public class ClientsTest extends BaseClientTest {
    private List<IClient> clients;
    private final int count = 5;
    private final int shard = 2;
    
    @Override
    public void start() throws IOException, InterruptedException {
        clients = new ArrayList<>(count);
        ClientFactory creator = new ClientFactory();
        for (int i = 0; i < count; i++) {
            clients.add(createClient(creator));
        }
    }
    
    @Override
    protected IClient createClient(ClientFactory creator) throws IOException, InterruptedException {
        IClient client;
        if (clients.size() % shard == 1){
            System.out.println("create client");
            client = newClient(creator);
        } else {
            System.out.println("create watch client");
            client = newWatchClient(creator);
        }
        return client;
    }
    
    private IClient newClient(ClientFactory creator) throws IOException, InterruptedException {
        return creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).start();
    }
    
    protected IClient newWatchClient(ClientFactory creator) throws IOException, InterruptedException {
        Listener listener = TestSupport.buildListener();
        return creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).watch(listener).start();
    }
    
    @Override
    public void stop() throws InterruptedException {
        for (IClient client : clients) {
            client.close();
        }
        clients = null;
    }
    
    @Test
    public void createRoot() throws KeeperException, InterruptedException {
        for (IClient client : clients) {
            super.createRoot(client);
        }
    }
    
    @Test
    public void createChild() throws KeeperException, InterruptedException {
        for (IClient client : clients) {
            super.createChild(client);
        }
    }
    
    @Test
    public void deleteBranch() throws KeeperException, InterruptedException {
        for (IClient client : clients) {
            super.deleteBranch(client);
        }
    }
    
    @Test
    public void isExisted() throws KeeperException, InterruptedException {
        for (IClient client : clients) {
            super.isExisted(client);
        }
    }
    
    @Test
    public void get() throws KeeperException, InterruptedException {
        for (IClient client : clients) {
            super.get(client);
        }
    }
    
    @Test
    public void asynGet() throws KeeperException, InterruptedException {
        for (IClient client : clients) {
            super.asynGet(client);
        }
    }
    
    @Test
    public void getChildrenKeys() throws KeeperException, InterruptedException {
        for (IClient client : clients) {
            super.getChildrenKeys(client);
        }
    }
    
    @Test
    public void persist() throws KeeperException, InterruptedException {
        for (IClient client : clients) {
            super.persist(client);
        }
    }
    
    @Test
    public void persistEphemeral() throws KeeperException, InterruptedException {
        for (IClient client : clients) {
            super.persistEphemeral(client);
        }
    }
    
    @Test
    public void delAllChildren() throws KeeperException, InterruptedException {
        for (IClient client : clients) {
            super.delAllChildren(client);
        }
    }
    
    @Test
    public void watch() throws KeeperException, InterruptedException {
        for (IClient client : clients) {
            super.watch(client);
        }
    }
    
    @Test
    public void close() throws Exception {
        for (IClient client : clients) {
            ZooKeeper zk = getZooKeeper(client);
            client.close();
            assert zk.getState() == ZooKeeper.States.CLOSED;
        }
    }
}
