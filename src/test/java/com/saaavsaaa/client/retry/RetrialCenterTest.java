package com.saaavsaaa.client.retry;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.retry.RetrialCenter;
import com.saaavsaaa.client.utility.section.Listener;
import com.saaavsaaa.client.zookeeper.ClientFactory;
import com.saaavsaaa.client.zookeeper.TestSupport;
import com.saaavsaaa.client.zookeeper.base.BaseClient;
import com.saaavsaaa.client.zookeeper.base.BaseClientTest;
import com.saaavsaaa.client.zookeeper.base.BaseProvider;
import com.saaavsaaa.client.zookeeper.operation.CreateCurrentOperation;
import com.saaavsaaa.client.zookeeper.strategy.StrategyType;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by aaa
 */
public class RetrialCenterTest{
    private IProvider provider;
    
    @Before
    public void start() throws IOException, InterruptedException {
        provider = new BaseProvider(createClient(), true);
        RetrialCenter.INSTANCE.start();
    }
    
    protected IClient createClient() throws IOException, InterruptedException {
        ClientFactory creator = new ClientFactory();
        Listener listener = TestSupport.buildListener();
        IClient client = creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).watch(listener).start();
        ((BaseClient)client).useExecStrategy(StrategyType.RETRY);
        return client;
    }
    
    @After
    public void stop(){
        
    }
    
    @Ignore
    @Test
    public void nothing(){
        
    }
    
//    @Test
    public void create(){
        String key = "a/b/bb";
//        RetrialCenter.INSTANCE.add(new CreateCurrentOperation(provider, path, value, createMode));
        /*client.createAllNeedPath(key, "bbb11", CreateMode.PERSISTENT);
        assert getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, key)*//*"/" + ROOT + "/" + key*//*, false) != null;
        client.deleteCurrentBranch(key);
        assert getZooKeeper(client).exists(PathUtil.getRealPath(TestSupport.ROOT, key)*//*"/" + ROOT + "/" + key*//*, false) == null;*/
    }
}
