package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.utility.section.Listener;
import org.apache.zookeeper.KeeperException;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaa
 */
public class ClientsTest extends BaseClientTest {
    private List<Client> clients;
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
    protected Client createClient(ClientFactory creator) throws IOException, InterruptedException {
        Client client;
        if (clients.size() % shard == 1){
            System.out.println("create client");
            client = newClient(creator);
        } else {
            System.out.println("create watch client");
            client = newWatchClient(creator);
        }
        return client;
    }
    
    private Client newClient(ClientFactory creator) throws IOException, InterruptedException {
        return creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).start();
    }
    
    protected Client newWatchClient(ClientFactory creator) throws IOException, InterruptedException {
        Listener listener = TestSupport.buildListener();
        return creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).watch(listener).start();
    }
    
    @Override
    public void stop() throws InterruptedException {
        for (Client client : clients) {
            client.close();
        }
        clients = null;
    }
    
    @Test
    public void createRoot() throws KeeperException, InterruptedException {
        for (Client client : clients) {
            super.createRoot(client);
        }
    }
    
    @Test
    public void createChild() throws KeeperException, InterruptedException {
        for (Client client : clients) {
            super.createChild(client);
        }
    }
    
    @Test
    public void deleteBranch() throws KeeperException, InterruptedException {
        for (Client client : clients) {
            super.deleteBranch(client);
        }
    }
    
    @Test
    public void isExisted() throws KeeperException, InterruptedException {
        for (Client client : clients) {
            super.isExisted(client);
        }
    }
    
    @Test
    public void get() throws KeeperException, InterruptedException {
        for (Client client : clients) {
            super.get(client);
        }
    }
    
    @Test
    public void asynGet() throws KeeperException, InterruptedException {
        for (Client client : clients) {
            super.asynGet(client);
        }
    }
    
    @Test
    public void getChildrenKeys() throws KeeperException, InterruptedException {
        for (Client client : clients) {
            super.getChildrenKeys(client);
        }
    }
    
    @Test
    public void persist() throws KeeperException, InterruptedException {
        for (Client client : clients) {
            super.persist(client);
        }
    }
    
    @Test
    public void persistEphemeral() throws KeeperException, InterruptedException {
        for (Client client : clients) {
            super.persistEphemeral(client);
        }
    }
    
    @Test
    public void delAllChildren() throws KeeperException, InterruptedException {
        for (Client client : clients) {
            super.delAllChildren(client);
        }
    }
    
    @Test
    public void watch() throws KeeperException, InterruptedException {
        for (Client client : clients) {
            super.watch(client);
        }
    }
    
    @Test
    public void close() throws Exception {
        for (Client client : clients) {
            super.close(client);
        }
    }
}
