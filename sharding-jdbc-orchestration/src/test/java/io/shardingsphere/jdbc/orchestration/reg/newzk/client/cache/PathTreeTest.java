package io.shardingsphere.jdbc.orchestration.reg.newzk.client.cache;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IClient;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.ClientFactory;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.base.TestSupport;
import io.shardingsphere.jdbc.orchestration.util.EmbedTestingServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/*
 * @author lidongbo
 */
public class PathTreeTest {
    private PathTree pathTree;
    private IClient testClient;
    
    @Before
    public void start() throws IOException, InterruptedException {
        EmbedTestingServer.start();
        ClientFactory creator = new ClientFactory();
        testClient = creator.setClientNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL)
                .newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).start();
    
        pathTree = new PathTree(TestSupport.ROOT, testClient);
    }
    
    @After
    public void stop() throws InterruptedException {
        pathTree.close();
        testClient.close();
    }
    
    @Test
    public void assertLoad() throws KeeperException, InterruptedException {
        final String keyB = "a/b/bb";
        final String valueB = "bbb11";
        testClient.createAllNeedPath(keyB, valueB, CreateMode.PERSISTENT);
        assert testClient.checkExists(keyB);
        final String keyC  = "a/c/cc";
        final String valueC = "ccc11";
        testClient.createAllNeedPath(keyC, valueC, CreateMode.PERSISTENT);
        assert testClient.checkExists(keyC);
        
        try {
            pathTree.load();
    
            assert valueB.equals(new String(pathTree.getValue(keyB)));
            assert valueC.equals(new String(pathTree.getValue(keyC)));
        } finally {
            testClient.deleteCurrentBranch(keyC);
            testClient.deleteCurrentBranch(keyB);
        }
    }
}
