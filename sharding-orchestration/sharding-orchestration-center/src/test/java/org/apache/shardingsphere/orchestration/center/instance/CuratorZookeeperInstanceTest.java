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

package org.apache.shardingsphere.orchestration.center.instance;

import org.apache.shardingsphere.orchestration.center.configuration.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.center.util.EmbedTestingServer;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CuratorZookeeperInstanceTest {
    
    private static CuratorZookeeperInstance curatorZookeeperInstance = new CuratorZookeeperInstance();
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        OrchestrationConfiguration configuration = new OrchestrationConfiguration(curatorZookeeperInstance.getType(), new Properties());
        configuration.setServerLists("127.0.0.1:3181");
        curatorZookeeperInstance.init(configuration);
    }
    
    @Test
    public void assertPersist() {
        curatorZookeeperInstance.persist("/test", "value1");
        assertThat(curatorZookeeperInstance.get("/test"), is("value1"));
    }
    
    @Test
    public void assertUpdate() {
        curatorZookeeperInstance.persist("/test", "value2");
        assertThat(curatorZookeeperInstance.get("/test"), is("value2"));
    }
    
    @Test
    public void assertPersistEphemeral() {
        curatorZookeeperInstance.persistEphemeral("/test/ephemeral", "value3");
        assertThat(curatorZookeeperInstance.get("/test/ephemeral"), is("value3"));
    }
    
    @Test
    public void assertGetChildrenKeys() {
        curatorZookeeperInstance.persist("/test/children/1", "value11");
        curatorZookeeperInstance.persist("/test/children/2", "value12");
        curatorZookeeperInstance.persist("/test/children/3", "value13");
        List<String> childrenKeys = curatorZookeeperInstance.getChildrenKeys("/test/children");
        assertThat(childrenKeys.size(), is(3));
    }
    
    @Test
    public void assertLock() {
        curatorZookeeperInstance.initLock("/test/lock1");
        assertThat(curatorZookeeperInstance.tryLock(), is(true));
    }
    
    @Test
    public void assertRelease() {
        curatorZookeeperInstance.initLock("/test/lock2");
        curatorZookeeperInstance.tryLock();
        curatorZookeeperInstance.tryRelease();
    }
    
    @Test(expected = IllegalMonitorStateException.class)
    public void assertReleaseWithoutLock() {
        curatorZookeeperInstance.initLock("/test/lock3");
        curatorZookeeperInstance.tryRelease();
    }
}
