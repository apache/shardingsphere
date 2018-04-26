package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.untils.Constants;
import com.saaavsaaa.client.untils.Listener;
import com.saaavsaaa.client.untils.PathUtil;
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
public class UsualClientTest extends BaseClientTest {
    
    @Test
    public void createRoot() throws KeeperException, InterruptedException {
        super.createRoot(testClient);
    }
    
    @Test
    public void createChild() throws KeeperException, InterruptedException {
        super.createChild(testClient);
    }
    
    @Test
    public void deleteBranch() throws KeeperException, InterruptedException {
        super.deleteBranch(testClient);
    }
    
    @Test
    public void isExisted() throws KeeperException, InterruptedException {
        super.isExisted(testClient);
    }
    
    @Test
    public void get() throws KeeperException, InterruptedException {
        super.get(testClient);
    }
    
    @Test
    public void asynGet() throws KeeperException, InterruptedException {
        super.asynGet(testClient);
    }
    
    @Test
    public void getChildrenKeys() throws KeeperException, InterruptedException {
        super.getChildrenKeys(testClient);
    }
    
    @Test
    public void persist() throws KeeperException, InterruptedException {
        super.persist(testClient);
    }
    
    @Test
    public void persistEphemeral() throws KeeperException, InterruptedException {
        super.persistEphemeral(testClient);
    }
    
    @Test
    public void delAllChildren() throws KeeperException, InterruptedException {
        super.delAllChildren(testClient);
    }
    
    @Test
    public void watch() throws KeeperException, InterruptedException {
        super.watch(testClient);
    }
    
    @Test
    public void close() throws Exception {
        super.close(testClient);
    }
}