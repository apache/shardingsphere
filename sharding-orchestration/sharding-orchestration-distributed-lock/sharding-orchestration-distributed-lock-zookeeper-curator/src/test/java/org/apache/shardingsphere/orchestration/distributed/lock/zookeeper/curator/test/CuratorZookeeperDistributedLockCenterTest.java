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

package org.apache.shardingsphere.orchestration.distributed.lock.zookeeper.curator.test;

import org.apache.shardingsphere.orchestration.distributed.lock.api.DistributedLockCenter;
import org.apache.shardingsphere.orchestration.distributed.lock.api.DistributedLockConfiguration;
import org.apache.shardingsphere.orchestration.distributed.lock.zookeeper.curator.CuratorZookeeperDistributedLockCenter;
import org.apache.shardingsphere.orchestration.distributed.lock.zookeeper.curator.util.EmbedTestingServer;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CuratorZookeeperDistributedLockCenterTest {
    
    private static DistributedLockCenter curatorZookeeperRegistryCenter = new CuratorZookeeperDistributedLockCenter();
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        DistributedLockConfiguration configuration = new DistributedLockConfiguration(curatorZookeeperRegistryCenter.getType(), new Properties());
        configuration.setServerLists("127.0.0.1:3181");
        curatorZookeeperRegistryCenter.init(configuration);
    }
    
    @Test
    public void assertPersist() {
        curatorZookeeperRegistryCenter.persist("/test", "value1");
        assertThat(curatorZookeeperRegistryCenter.get("/test"), is("value1"));
    }
    
    @Test
    public void assertUpdate() {
        curatorZookeeperRegistryCenter.persist("/test", "value2");
        assertThat(curatorZookeeperRegistryCenter.get("/test"), is("value2"));
    }
    
    @Test
    public void assertPersistEphemeral() {
        curatorZookeeperRegistryCenter.persist("/test/ephemeral", "value3");
        assertThat(curatorZookeeperRegistryCenter.get("/test/ephemeral"), is("value3"));
    }
    
    @Test
    public void assertLock() {
        curatorZookeeperRegistryCenter.initLock("/test/lock1");
        assertThat(curatorZookeeperRegistryCenter.tryLock(), is(true));
    }
    
    @Test
    public void assertRelease() {
        curatorZookeeperRegistryCenter.initLock("/test/lock2");
        curatorZookeeperRegistryCenter.tryLock();
        curatorZookeeperRegistryCenter.tryRelease();
    }
    
    @Test(expected = IllegalMonitorStateException.class)
    public void assertReleaseWithoutLock() {
        curatorZookeeperRegistryCenter.initLock("/test/lock3");
        curatorZookeeperRegistryCenter.tryRelease();
    }
}
