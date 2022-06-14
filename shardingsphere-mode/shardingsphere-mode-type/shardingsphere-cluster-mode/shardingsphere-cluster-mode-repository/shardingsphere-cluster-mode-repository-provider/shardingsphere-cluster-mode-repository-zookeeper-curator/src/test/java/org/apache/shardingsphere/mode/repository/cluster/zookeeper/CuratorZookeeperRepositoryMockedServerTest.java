/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.mode.repository.cluster.zookeeper;

import org.apache.curator.test.TestingServer;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Lock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class CuratorZookeeperRepositoryMockedServerTest {
    
    private static final CuratorZookeeperRepository REPOSITORY = new CuratorZookeeperRepository();
    
    private static TestingServer server;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        server = new TestingServer(2181, true);
        server.start();
        REPOSITORY.init(createConfiguration());
    }
    
    private static ClusterPersistRepositoryConfiguration createConfiguration() {
        Properties properties = new Properties();
        properties.put("retryIntervalMilliseconds", 500);
        properties.put("maxRetries", 3);
        properties.put("timeToLiveSeconds", 60);
        properties.put("operationTimeoutMilliseconds", 500);
        return new ClusterPersistRepositoryConfiguration("Cluster", "governance_ds", "localhost:2181", properties);
    }
    
    @Test
    public void assertGet() {
        assertNull(REPOSITORY.get("/test/get"));
    }
    
    @Test
    public void assertGetChildrenKeys() {
        REPOSITORY.persist("/test/children_key1", "127.0.0.1");
        REPOSITORY.persist("/test/children_key2", "127.0.0.1");
        REPOSITORY.persist("/test/children_key3", "127.0.0.1");
        List<String> childrenKeys = REPOSITORY.getChildrenKeys("/test");
        assertThat(childrenKeys.size(), is(3));
        Iterator<String> iterator = childrenKeys.iterator();
        assertThat(iterator.next(), is("children_key3"));
        assertThat(iterator.next(), is("children_key2"));
        assertThat(iterator.next(), is("children_key1"));
    }
    
    @Test
    public void assertPersist() {
        String key = "/test/persist";
        REPOSITORY.persist(key, "127.0.0.1");
        assertThat(REPOSITORY.get(key), is("127.0.0.1"));
    }
    
    @Test
    public void assertPersistEphemeral() {
        String key = "/test/ephemeral_key";
        REPOSITORY.persistEphemeral(key, "127.0.0.1");
        assertThat(REPOSITORY.get(key), is("127.0.0.1"));
        REPOSITORY.delete(key);
        assertNull(REPOSITORY.get(key));
    }
    
    @Test
    public void assertGetSequentialId() {
        String key = "/sequential/sequential_id";
        String sequentialIdStr = REPOSITORY.getSequentialId(key, "127.0.0.1");
        assertNotNull(sequentialIdStr);
        Long sequentialId = Long.valueOf(sequentialIdStr);
        assertThat(sequentialId, is(0L));
    }
    
    @Test
    public void assertGetInternalMutexLock() {
        String key = "/lock/database/locks/sharding_db1";
        Lock lock = REPOSITORY.getInternalMutexLock(key);
        lock.lock();
        List<String> leases = REPOSITORY.getChildrenKeys("/lock/database/locks/sharding_db1");
        assertThat(leases.size(), is(2));
        leases = REPOSITORY.getChildrenKeys("/lock/database/locks/sharding_db1/leases");
        assertThat(leases.size(), is(1));
        lock.unlock();
        leases = REPOSITORY.getChildrenKeys("/lock/database/locks/sharding_db1/leases");
        assertTrue(leases.isEmpty());
    }
    
    @Test
    public void assertGetInternalReentrantMutexLock() {
        String key = "/lock/database/locks/sharding_db2";
        Lock lock = REPOSITORY.getInternalReentrantMutexLock(key);
        lock.lock();
        List<String> leases = REPOSITORY.getChildrenKeys("/lock/database/locks/sharding_db2");
        assertThat(leases.size(), is(1));
        lock.unlock();
        leases = REPOSITORY.getChildrenKeys("/lock/database/locks/sharding_db2");
        assertTrue(leases.isEmpty());
    }
    
    @AfterClass
    public static void closeAfterClass() throws IOException {
        REPOSITORY.close();
        server.stop();
    }
}
