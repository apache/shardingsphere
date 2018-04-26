package com.saaavsaaa.client.zookeeper;

import org.apache.zookeeper.KeeperException;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaa on 18-4-26.
 */
public class ClientsTest extends BaseClientTest {
    private List<Client> clients;
    
    @Override
    public void start() throws IOException, InterruptedException {
        clients = new ArrayList<>(2);
        ClientFactory creator = new ClientFactory();
        clients.add(createClient(creator));
        clients.add(createWatchClient(creator));
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
