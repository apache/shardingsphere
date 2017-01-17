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

import com.dangdang.ddframe.rdb.sharding.id.generator.IdGenerator;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 通过zookeeper维护工作进程Id，启动时会在"/snowflake/appName(这里可以是应用名，服务名，表名)/"节点下注册工作进程Id.
 * ，例如注册节点"/snowflake/default/7"，则workId为7.
 * 运行时需要设置三个参数：
 * <pre>
 * zkNodes（zookeeper节点，IP:PORT逗号分隔，如"192.168.1.100:2181,127.0.0.1:2181"）: 系统变量{@code sjdbc.zk.id.generator.zk.nodes} > 环境变量{@code SJDBC_ZK_ID_GENERATOR_ZK_NODES}
 * appName（可以是表名，如"default_table"）: 系统变量{@code sjdbc.zk.id.generator.app.name} > 环境变量{@code SJDBC_ZK_ID_GENERATOR_APP_NAME}
 * authority（ZK节点权限，如"admin:password"，可以不设置）: 系统变量{@code sjdbc.zk.id.generator.zk.authority} > 环境变量{@code SJDBC_ZK_ID_GENERATOR_ZK_AUTHORITY}
 * </pre>
 * 如无"/snowflake/appName/"节点，会先创建节点。
 * 每次注册新节点时，会将当前更新节点写入应用根节点
 * ，如注册的新节点为"/snowflake/appName/108/"，则将108写入"/snowflake/appName/"节点。
 * 注册时，先获取最后一次写入的节点ID，即"/snowflake/appName/"节点数据，
 * ，如"/snowflake/appName/"节点值为108，则创建新节点"/snowflake/appName/109/"，并更新"/snowflake/appName/"节点为109。
 * 如果109节点以存在，则往后顺延到110节点；如顺延到1023节点还未找到空节点，则从0开始重新遍历；如0-1023节点都被占用，则抛出异常
 * 如果与zookeeper失去连接，将触发监听器，重新去zookeeper中注册节点ID。
 *
 * @author DonneyYoung
 */
public class ZkIdGenerator implements IdGenerator {

    private final CommonSelfIdGenerator commonSelfIdGenerator = new CommonSelfIdGenerator();

    static {
        initWorkerId();
    }

    static void initWorkerId() {
        try {
            String zkNodes = System.getProperty("sjdbc.zk.id.generator.zk.nodes");
            String appName = System.getProperty("sjdbc.zk.id.generator.app.name");
            String authority = System.getProperty("sjdbc.zk.id.generator.zk.authority");
            if (!Strings.isNullOrEmpty(zkNodes) && !Strings.isNullOrEmpty(appName)) {
                ZkClient.initZkClient(zkNodes, authority, ZkClient.SNOWFLAKE_URL + "/" + appName);
                return;
            }
            zkNodes = System.getenv("SJDBC_ZK_ID_GENERATOR_ZK_NODES");
            appName = System.getenv("SJDBC_ZK_ID_GENERATOR_APP_NAME");
            authority = System.getenv("SJDBC_ZK_ID_GENERATOR_ZK_AUTHORITY");
            if (!Strings.isNullOrEmpty(zkNodes) && !Strings.isNullOrEmpty(appName)) {
                ZkClient.initZkClient(zkNodes, authority, ZkClient.SNOWFLAKE_URL + "/" + appName);
                return;
            }
        } catch (final Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        throw new IllegalArgumentException("zkNodes and appName can not be empty.");
    }

    @Override
    public Number generateId() {
        return commonSelfIdGenerator.generateId();
    }

    private static class ZkClient {

        // 向Zookeeper注册的根节点
        private static final String SNOWFLAKE_URL = "/snowflake";

        // 向Zookeeper注册的Snowflake Node节点
        private static final String SNOWFLAKE_NODE = "/node";

        // 向Zookeeper注册的Snowflake Config节点
        private static final String SNOWFLAKE_CONFIG = "/config";

        // 向Zookeeper注册的Snowflake Config节点
        private static final String SNOWFLAKE_ORDER = "/order";

        // 授权方式
        private static final String AUTHORIZATION = "digest";

        private static final long WORKER_ID_MAX_VALUE = 1024L;

        // Session过期时间
        private static final int SESSION_TIMEOUT_MS = 60 * 1000;

        // 连接过期时间
        private static final int CONNECTION_TIMEOUT_MS = 3000;

        private static final int BASE_SLEEP_TIME_MS = 1000;

        private static final int LOCK_WAIT_TIME_MS = 30000;

        // Curator客户端
        private static CuratorFramework client;

        // 注册节点监听
        private static TreeCache treeCache;

        // 分布式锁
        private static InterProcessMutex lock;

        // 节点创建时间
        private static volatile long pathCreatedTime;

        private static void closeClient() {
            if (null != client && null == client.getState()) {
                client.close();
            }
            client = null;
        }

        private static void initZkClient(final String zkNodes, final String authority, final String appNode) throws Exception {
            if (null != client) {
                closeClient();
            }
            CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                    .connectString(zkNodes)
                    .sessionTimeoutMs(SESSION_TIMEOUT_MS)
                    .connectionTimeoutMs(CONNECTION_TIMEOUT_MS)
                    .canBeReadOnly(false)
                    .retryPolicy(new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, Integer.MAX_VALUE))
                    .namespace(null)
                    .defaultData(null);
            if (authority != null && authority.length() > 0) {
                ACLProvider aclProvider = new ACLProvider() {
                    private List<ACL> acls;

                    @Override
                    public List<ACL> getDefaultAcl() {
                        if (acls == null) {
                            ArrayList<ACL> acls = ZooDefs.Ids.CREATOR_ALL_ACL;
                            acls.clear();
                            acls.add(new ACL(ZooDefs.Perms.ALL, new Id(AUTHORIZATION, authority)));
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
                builder.aclProvider(aclProvider).authorization(AUTHORIZATION, authority.getBytes());
            }
            client = builder.build();
            client.start();
            doRegister(appNode);
        }

        private static void doRegister(final String appPath) throws Exception {
            if (null == client.checkExists().forPath(appPath)) {
                try {
                    client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(appPath);
                } catch (final KeeperException.NodeExistsException ignored) {
                }
            }
            try {
                lock = new InterProcessMutex(client, appPath);
                if (!lock.acquire(LOCK_WAIT_TIME_MS, TimeUnit.MILLISECONDS)) {
                    throw new TimeoutException(String.format("acquire lock failed after %s ms.", LOCK_WAIT_TIME_MS));
                }
                if (null == client.checkExists().forPath(appPath + SNOWFLAKE_NODE)) {
                    client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(appPath + SNOWFLAKE_NODE);
                }
                if (null == client.checkExists().forPath(appPath + SNOWFLAKE_CONFIG + SNOWFLAKE_ORDER)) {
                    client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(appPath + SNOWFLAKE_CONFIG + SNOWFLAKE_ORDER);
                }
                Set<Integer> orderIdSet = checkAndInitOrder(new String(client.getData().forPath(appPath + SNOWFLAKE_CONFIG + SNOWFLAKE_ORDER)));
                Set<Integer> nodeIdSet = new LinkedHashSet<>(Lists.transform(client.getChildren().forPath(appPath + SNOWFLAKE_NODE), new Function<String, Integer>() {
                    @Override
                    public Integer apply(final String nodeIdStr) {
                        return Integer.parseInt(nodeIdStr);
                    }
                }));
                for (Integer each : orderIdSet) {
                    if (!nodeIdSet.contains(each)) {
                        orderIdSet.remove(each);
                        orderIdSet.add(each);
                        final String nodePath = appPath + SNOWFLAKE_NODE + "/" + each;
                        String nodeDate = String.format("ip:%s\r\nhostName:%s\r\npid:%s\r\n", InetAddress.getLocalHost().getHostAddress(), InetAddress.getLocalHost().getHostName(), ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
                        client.inTransaction().create().withMode(CreateMode.EPHEMERAL).forPath(nodePath)
                                .and().setData().forPath(nodePath, nodeDate.getBytes())
                                .and().setData().forPath(appPath + SNOWFLAKE_CONFIG + SNOWFLAKE_ORDER, orderIdSet.toString().getBytes())
                                .and().commit();
                        pathCreatedTime = client.checkExists().forPath(nodePath).getCtime();
                        ZkClient.treeCache = new TreeCache(client, nodePath);
                        final TreeCache treeCache = ZkClient.treeCache;
                        treeCache.getListenable().addListener(
                                new TreeCacheListener() {
                                    @Override
                                    public void childEvent(final CuratorFramework curatorFramework, final TreeCacheEvent treeCacheEvent) throws Exception {
                                        long pathTime;
                                        try {
                                            pathTime = curatorFramework.checkExists().forPath(nodePath).getCtime();
                                        } catch (final Exception e) {
                                            pathTime = 0;
                                        }
                                        if (pathCreatedTime != pathTime) {
                                            doRegister(appPath);
                                            treeCache.close();
                                        }
                                    }
                                }
                        );
                        treeCache.start();
                        CommonSelfIdGenerator.setWorkerId(Long.valueOf(each));
                        return;
                    }
                }
                throw new IllegalStateException(String.format("The snowflake node is full! The max node amount is %s.", WORKER_ID_MAX_VALUE));
            } catch (final Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            } finally {
                try {
                    lock.release();
                } catch (final IllegalMonitorStateException ignored) {
                }
            }
        }

        private static Set<Integer> checkAndInitOrder(final String orderIdSetStr) {
            Set<Integer> result;
            try {
                String[] orderIdSplit = orderIdSetStr.replace("[", "").replace("]", "").replace(" ", "").split(",");
                result = new LinkedHashSet<>(Lists.transform(Arrays.asList(orderIdSplit), new Function<String, Integer>() {
                    @Override
                    public Integer apply(final String orderIdStr) {
                        return Integer.parseInt(orderIdStr);
                    }
                }));
                if (result.size() != 1024) {
                    throw new IllegalArgumentException();
                }
                for (Integer each : result) {
                    if (each < 0 || each >= WORKER_ID_MAX_VALUE) {
                        throw new IllegalArgumentException();
                    }
                }
                return result;
            } catch (final IllegalArgumentException ignored) {
            }
            result = new LinkedHashSet<>();
            for (int i = 0; i < WORKER_ID_MAX_VALUE; i++) {
                result.add(i);
            }
            return result;
        }
    }
}
