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

package org.apache.shardingsphere.orchestration.config.zookeeper.curator.test;

import org.apache.shardingsphere.orchestration.config.zookeeper.curator.util.EmbedTestingServer;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenter;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import org.apache.shardingsphere.orchestration.reg.zookeeper.curator.CuratorZookeeperRegistryCenter;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class CuratorZookeeperRegistryCenterTest {
    
    private static RegistryCenter curatorZookeeperRegistryCenter = new CuratorZookeeperRegistryCenter();
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        RegistryCenterConfiguration configuration = new RegistryCenterConfiguration(curatorZookeeperRegistryCenter.getType(), new Properties());
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
    public void assertGetDirectly() {
        curatorZookeeperRegistryCenter.persist("/test", "value4");
        assertThat(curatorZookeeperRegistryCenter.getDirectly("/test"), is("value4"));
    }
    
    @Test
    public void assertIsExisted() {
        curatorZookeeperRegistryCenter.persist("/test/existed", "value5");
        assertThat(curatorZookeeperRegistryCenter.isExisted("/test/existed"), is(true));
    }
    
    @Test
    public void assertGetChildrenKeys() {
        curatorZookeeperRegistryCenter.persist("/test/children/1", "value11");
        curatorZookeeperRegistryCenter.persist("/test/children/2", "value12");
        curatorZookeeperRegistryCenter.persist("/test/children/3", "value13");
        List<String> childrenKeys = curatorZookeeperRegistryCenter.getChildrenKeys("/test/children");
        assertThat(childrenKeys.size(), is(3));
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
