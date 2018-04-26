package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.untils.Constants;
import com.saaavsaaa.client.untils.Listener;
import org.apache.zookeeper.KeeperException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaa on 18-4-26.
 */
public class ClientsTest extends BaseClientTest {
    private List<Client> clients;
    
    @Before
    @Override
    public void start() throws IOException, InterruptedException {
        clients = new ArrayList<>(2);
        ClientFactory creator = new ClientFactory();
        clients.add(createClient(creator));
        clients.add(createWatchClient(creator));
    }
    
    @After
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
            client.createNamespace();
            assert client.getZooKeeper().exists(Constants.PATH_SEPARATOR + TestSupport.ROOT, false) != null;
            client.deleteNamespace();
            assert client.getZooKeeper().exists(Constants.PATH_SEPARATOR + TestSupport.ROOT, false) == null;
        }
    }
}
