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

package org.apache.shardingsphere.orchestration.center.configuration;

import java.util.Properties;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class InstanceConfigurationTest {
    
    @Test
    public void assertConstructorWithType() {
        String type = "zookeeper";
        assertThat(new InstanceConfiguration(type).getType(), is(type));
    }
    
    @Test
    public void assertConstructorWithTypeAndProperties() {
        String type = "zookeeper";
        Properties properties = new Properties();
        InstanceConfiguration instanceConfiguration = new InstanceConfiguration(type, properties);
        assertThat(instanceConfiguration.getType(), is(type));
        assertThat(instanceConfiguration.getProperties(), is(properties));
    }
    
    @Test
    public void assertCenterType() {
        String type = "zookeeper";
        String orchestrationType = "config_center";
        InstanceConfiguration instanceConfiguration = new InstanceConfiguration(type);
        instanceConfiguration.setOrchestrationType(orchestrationType);
        assertThat(instanceConfiguration.getOrchestrationType(), is(orchestrationType));
    }
    
    @Test
    public void assertServerLists() {
        String type = "zookeeper";
        String serverLists = "127.0.0.1:2181,127.0.0.1:2182";
        InstanceConfiguration instanceConfiguration = new InstanceConfiguration(type);
        instanceConfiguration.setServerLists(serverLists);
        assertThat(instanceConfiguration.getServerLists(), is(serverLists));
    }
    
    @Test
    public void assertNamespace() {
        String type = "zookeeper";
        String namespace = "orchestration";
        InstanceConfiguration instanceConfiguration = new InstanceConfiguration(type);
        instanceConfiguration.setNamespace(namespace);
        assertThat(instanceConfiguration.getNamespace(), is(namespace));
    }
}
