package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.section.Listener;
import com.saaavsaaa.client.zookeeper.base.BaseClient;
import com.saaavsaaa.client.zookeeper.strategy.StrategyType;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by aaa
 */
public class ContentionStrategyTest extends UsualClientTest {
    @Override
    protected IClient createClient(final ClientFactory creator) throws IOException, InterruptedException {
        Listener listener = TestSupport.buildListener();
        IClient client = creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).watch(listener).start();
        ((BaseClient)client).useExecStrategy(StrategyType.CONTENTION);
        return client;
    }
    
    //todo test node contention case
    
    @Test
    public void deleteBranch() throws KeeperException, InterruptedException {
        String keyB = "a/b/bb";
        testClient.createAllNeedPath(keyB, "bbb11", CreateMode.PERSISTENT);
        assert zooKeeper.exists(PathUtil.getRealPath(TestSupport.ROOT, keyB), false) != null;
        String keyC  = "a/c/cc";
        testClient.createAllNeedPath(keyC, "ccc11", CreateMode.PERSISTENT);
        assert zooKeeper.exists(PathUtil.getRealPath(TestSupport.ROOT, keyC), false) != null;
        testClient.deleteCurrentBranch(keyC);
        assert zooKeeper.exists(PathUtil.getRealPath(TestSupport.ROOT, keyC), false) == null;
        assert zooKeeper.exists(PathUtil.getRealPath(TestSupport.ROOT, "a"), false) != null;
        testClient.deleteCurrentBranch(keyB); // because Constants.CHANGING_KEY, root still exist
        List<String> children = zooKeeper.getChildren(PathUtil.checkPath(TestSupport.ROOT), false);
        assert children.size() == 0;
        deleteRoot(testClient);
        assert zooKeeper.exists(PathUtil.checkPath(TestSupport.ROOT), false) == null;
        testClient.createAllNeedPath(keyB, "bbb11", CreateMode.PERSISTENT);
        assert zooKeeper.exists(PathUtil.getRealPath(TestSupport.ROOT, keyB), false) != null;
        testClient.deleteCurrentBranch(keyB);
        children = zooKeeper.getChildren(PathUtil.checkPath(TestSupport.ROOT), false);
        assert children.size() == 0;
        deleteRoot(testClient);
        assert zooKeeper.exists(PathUtil.checkPath(TestSupport.ROOT), false) == null;
    }
}
