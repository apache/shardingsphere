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

package org.apache.shardingsphere.orchestration.center.config;

import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class CenterConfigurationTest {
    
    @Test
    public void assertConstructorWithType() {
        String type = "zookeeper";
        assertThat(new CenterConfiguration(type).getType(), is(type));
    }
    
    @Test
    public void assertConstructorWithTypeAndProperties() {
        String type = "zookeeper";
        Properties properties = new Properties();
        CenterConfiguration configuration = new CenterConfiguration(type, properties);
        assertThat(configuration.getType(), is(type));
        assertThat(configuration.getProperties(), is(properties));
    }
    
    @Test
    public void assertCenterType() {
        String type = "zookeeper";
        String orchestrationType = "config_center";
        CenterConfiguration configuration = new CenterConfiguration(type);
        configuration.setOrchestrationType(orchestrationType);
        assertThat(configuration.getOrchestrationType(), is(orchestrationType));
    }
    
    @Test
    public void assertServerLists() {
        String type = "zookeeper";
        String serverLists = "127.0.0.1:2181,127.0.0.1:2182";
        CenterConfiguration configuration = new CenterConfiguration(type);
        configuration.setServerLists(serverLists);
        assertThat(configuration.getServerLists(), is(serverLists));
    }
    
    @Test
    public void assertNamespace() {
        String type = "zookeeper";
        String namespace = "orchestration";
        CenterConfiguration configuration = new CenterConfiguration(type);
        configuration.setNamespace(namespace);
        assertThat(configuration.getNamespace(), is(namespace));
    }
}
