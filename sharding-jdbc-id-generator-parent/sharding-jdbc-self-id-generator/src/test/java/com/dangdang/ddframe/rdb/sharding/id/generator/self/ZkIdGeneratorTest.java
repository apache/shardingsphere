/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.id.generator.self;

import com.dangdang.ddframe.rdb.sharding.id.generator.self.time.AbstractClock;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.test.Timing;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ZkIdGeneratorTest {

    @Rule
    public final EnvironmentVariables ENVIRONMENT_VARIABLES = new EnvironmentVariables();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static final String SHARDING_JDBC_ROOT = "/sharding-jdbc";

    private static final String SHARDING_JDBC_NODE = "/node";

    private static String appName = "defaultapp";

    private static String appPath = SHARDING_JDBC_ROOT + "/" + appName;

    private static CuratorFramework client;

    private static String connectString;

    @BeforeClass
    public static void init() throws Exception {
        TestingServer server = new TestingServer();
        connectString = server.getConnectString();
        System.setProperty("sjdbc.zk.id.generator.zk.nodes", connectString);
        System.setProperty("sjdbc.zk.id.generator.app.name", "init");
        new ZkIdGenerator();
    }

    @Before
    public void setup() {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .sessionTimeoutMs(new Timing().session())
                .connectionTimeoutMs(1)
                .retryPolicy(new ExponentialBackoffRetry(1000, 1000));
        ACLProvider aclProvider = new ACLProvider() {
            private List<ACL> acls;

            @Override
            public List<ACL> getDefaultAcl() {
                if (acls == null) {
                    ArrayList<ACL> acls = ZooDefs.Ids.CREATOR_ALL_ACL;
                    acls.clear();
                    acls.add(new ACL(ZooDefs.Perms.ALL, new Id("digest", "admin:admin123")));
                    acls.add(new ACL(ZooDefs.Perms.ALL, new Id("world", "anyone")));
                    this.acls = acls;
                }
                return acls;
            }

            @Override
            public List<ACL> getAclForPath(final String path) {
                return acls;
            }
        };
        builder.aclProvider(aclProvider).authorization("digest", "admin:admin123".getBytes());
        client = builder.build();
        client.start();
        System.setProperty("sjdbc.zk.id.generator.zk.nodes", connectString);
        System.setProperty("sjdbc.zk.id.generator.app.name", appName);
    }

    @After
    public void clear() throws Exception {
        Class<?>[] declaringClass = ZkIdGenerator.class.getDeclaredClasses();
        for (Class<?> each : declaringClass) {
            if (each.getSimpleName().equals("ZkClient")) {
                Method closeClient = each.getDeclaredMethod("closeClient", (Class<?>[]) null);
                closeClient.setAccessible(true);
                closeClient.invoke(each, (Object[]) null);
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CommonSelfIdGenerator.setWorkerId(0L);
        if (null != checkExists(appPath)) {
            deleteNode(appPath);
        }
        ZkIdGeneratorTest.client.close();
        System.setProperty("sjdbc.zk.id.generator.zk.nodes", "");
        System.setProperty("sjdbc.zk.id.generator.app.name", "");
        ENVIRONMENT_VARIABLES.set("SJDBC_ZK_ID_GENERATOR_ZK_NODES", "");
        ENVIRONMENT_VARIABLES.set("SJDBC_ZK_ID_GENERATOR_APP_NAME", "");
    }

    @Test
    public void generateId() throws Exception {
        CommonSelfIdGenerator.setClock(AbstractClock.systemClock());
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        final int taskNumber = threadNumber << 2;
        final ZkIdGenerator idGenerator = new ZkIdGenerator();
        ZkIdGenerator.initWorkerId();
        Set<Long> hashSet = new HashSet<>();
        for (int i = 0; i < taskNumber; i++) {
            hashSet.add(executor.submit(new Callable<Long>() {
                @Override
                public Long call() throws Exception {
                    return (Long) idGenerator.generateId();
                }
            }).get());
        }
        Assert.assertThat(hashSet.size(), is(taskNumber));
    }

    @Test
    public void testBuildByNull() throws Exception {
        System.setProperty("sjdbc.zk.id.generator.zk.nodes", "");
        System.setProperty("sjdbc.zk.id.generator.app.name", "");
        ENVIRONMENT_VARIABLES.set("SJDBC_ZK_ID_GENERATOR_ZK_NODES", "");
        ENVIRONMENT_VARIABLES.set("SJDBC_ZK_ID_GENERATOR_APP_NAME", "");
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("zkNodes and appName can not be empty.");
        ZkIdGenerator.initWorkerId();
    }

    @Test
    public void testBuildByProperty() throws Exception {
        System.setProperty("sjdbc.zk.id.generator.zk.nodes", connectString);
        System.setProperty("sjdbc.zk.id.generator.app.name", appName);
        ENVIRONMENT_VARIABLES.set("SJDBC_ZK_ID_GENERATOR_ZK_NODES", "");
        ENVIRONMENT_VARIABLES.set("SJDBC_ZK_ID_GENERATOR_APP_NAME", "");
        ZkIdGenerator.initWorkerId();
        assertThat("ShardingJdbcNode is not created!", checkExists(appPath + SHARDING_JDBC_NODE + "/" + String.valueOf(0L)), notNullValue());
        assertThat("WorkerId is not equal!", CommonSelfIdGenerator.getWorkerId(), equalTo(0L));
    }

    @Test
    public void testBuildByEnvironment() throws Exception {
        System.setProperty("sjdbc.zk.id.generator.zk.nodes", "");
        System.setProperty("sjdbc.zk.id.generator.app.name", "");
        ENVIRONMENT_VARIABLES.set("SJDBC_ZK_ID_GENERATOR_ZK_NODES", connectString);
        ENVIRONMENT_VARIABLES.set("SJDBC_ZK_ID_GENERATOR_APP_NAME", appName);
        ZkIdGenerator.initWorkerId();
        assertThat("ShardingJdbcNode is not created!", checkExists(appPath + SHARDING_JDBC_NODE + "/" + String.valueOf(0L)), notNullValue());
        assertThat("WorkerId is not equal!", CommonSelfIdGenerator.getWorkerId(), equalTo(0L));
    }


    @Test
    public void reRegisterTest() throws Exception {
        ZkIdGenerator.initWorkerId();
        assertThat("ShardingJdbcNode is not created!", checkExists(appPath + SHARDING_JDBC_NODE + "/" + String.valueOf(0L)), notNullValue());
        assertThat("WorkerId is not equal!", CommonSelfIdGenerator.getWorkerId(), equalTo(0L));
        deleteNode(appPath + SHARDING_JDBC_NODE + "/" + String.valueOf(0L));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertThat("ShardingJdbcNode is not closed!", checkExists(appPath + SHARDING_JDBC_NODE + "/" + String.valueOf(0L)), equalTo(null));
        assertThat("ShardingJdbcNode is not created!", checkExists(appPath + SHARDING_JDBC_NODE + "/" + String.valueOf(1L)), notNullValue());
        assertThat("WorkerId is not equal!", CommonSelfIdGenerator.getWorkerId(), equalTo(1L));
    }


    @Test
    public void whenFullTest() throws Exception {
        for (long i = 0L; i < 1024L; i++) {
            createEphemeral(appPath + SHARDING_JDBC_NODE + "/" + String.valueOf(i));
        }
        exception.expect(IllegalStateException.class);
        exception.expectMessage("The sharding-jdbc node is full! The max node amount is 1024.");
        ZkIdGenerator.initWorkerId();
    }

    @Test
    public void authTest() throws Exception {
        System.setProperty("sjdbc.zk.id.generator.zk.authority", "admin:admin123");
        createPersistent(appPath);
        setAcl(appPath, "digest", "admin:admin123");
        ZkIdGenerator.initWorkerId();
        assertThat("ShardingJdbcNode is not created!", checkExists(appPath + SHARDING_JDBC_NODE + "/" + String.valueOf(0L)), notNullValue());
        assertThat("WorkerId is not equal!", CommonSelfIdGenerator.getWorkerId(), equalTo(0L));
    }

    private static void createEphemeral(String path) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static void createPersistent(String path) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static void setAcl(String path, String scheme, String idPassword) {
        try {
            Id id = new Id(scheme, DigestAuthenticationProvider.generateDigest(idPassword));
            ACL acl = new ACL(ZooDefs.Perms.ALL, id);
            List<ACL> acls = new ArrayList<>();
            acls.add(acl);
            client.setACL().withACL(acls).forPath(path);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static Void deleteNode(String path) {
        try {
            return client.delete().deletingChildrenIfNeeded().forPath(path);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static Stat checkExists(String path) {
        try {
            return client.checkExists().forPath(path);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
