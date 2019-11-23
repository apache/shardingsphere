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

import org.apache.shardingsphere.orchestration.config.api.ConfigCenter;
import org.apache.shardingsphere.orchestration.config.api.ConfigCenterConfiguration;
import org.apache.shardingsphere.orchestration.config.zookeeper.curator.CuratorZookeeperConfigCenter;
import org.apache.shardingsphere.orchestration.config.zookeeper.curator.util.EmbedTestingServer;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class CuratorZookeeperConfigCenterTest {
    
    private static ConfigCenter curatorZookeeperConfigCenter = new CuratorZookeeperConfigCenter();
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        ConfigCenterConfiguration configuration = new ConfigCenterConfiguration(curatorZookeeperConfigCenter.getType(), new Properties());
        configuration.setServerLists("127.0.0.1:3181");
        curatorZookeeperConfigCenter.init(configuration);
    }
    
    @Test
    public void assertPersist() {
        curatorZookeeperConfigCenter.persist("/test", "value1");
        assertThat(curatorZookeeperConfigCenter.get("/test"), is("value1"));
    }
    
    @Test
    public void assertUpdate() {
        curatorZookeeperConfigCenter.persist("/test", "value2");
        assertThat(curatorZookeeperConfigCenter.get("/test"), is("value2"));
    }
    
    @Test
    public void assertPersistEphemeral() {
        curatorZookeeperConfigCenter.persist("/test/ephemeral", "value3");
        assertThat(curatorZookeeperConfigCenter.get("/test/ephemeral"), is("value3"));
    }
    
    @Test
    public void assertGetDirectly() {
        curatorZookeeperConfigCenter.persist("/test", "value4");
        assertThat(curatorZookeeperConfigCenter.getDirectly("/test"), is("value4"));
    }
    
    @Test
    public void assertIsExisted() {
        curatorZookeeperConfigCenter.persist("/test/existed", "value5");
        assertThat(curatorZookeeperConfigCenter.isExisted("/test/existed"), is(true));
    }
    
    @Test
    public void assertGetChildrenKeys() {
        curatorZookeeperConfigCenter.persist("/test/children/1", "value11");
        curatorZookeeperConfigCenter.persist("/test/children/2", "value12");
        curatorZookeeperConfigCenter.persist("/test/children/3", "value13");
        List<String> childrenKeys = curatorZookeeperConfigCenter.getChildrenKeys("/test/children");
        assertThat(childrenKeys.size(), is(3));
    }
}
