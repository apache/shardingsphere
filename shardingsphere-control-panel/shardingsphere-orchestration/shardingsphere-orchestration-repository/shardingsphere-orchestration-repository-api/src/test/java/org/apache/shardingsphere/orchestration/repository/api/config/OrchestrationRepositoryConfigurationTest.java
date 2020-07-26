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

package org.apache.shardingsphere.orchestration.repository.api.config;

import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class OrchestrationRepositoryConfigurationTest {
    
    @Test
    public void assertConstructorWithType() {
        String type = "zookeeper";
        assertThat(new OrchestrationRepositoryConfiguration(type, new Properties()).getType(), is(type));
    }
    
    @Test
    public void assertConstructorWithTypeAndProperties() {
        String type = "zookeeper";
        Properties props = new Properties();
        OrchestrationRepositoryConfiguration configuration = new OrchestrationRepositoryConfiguration(type, props);
        assertThat(configuration.getType(), is(type));
        assertThat(configuration.getProps(), is(props));
    }
    
    @Test
    public void assertServerLists() {
        String type = "zookeeper";
        String serverLists = "127.0.0.1:2181,127.0.0.1:2182";
        OrchestrationRepositoryConfiguration configuration = new OrchestrationRepositoryConfiguration(type, new Properties());
        configuration.setServerLists(serverLists);
        assertThat(configuration.getServerLists(), is(serverLists));
    }
    
    @Test
    public void assertNamespace() {
        String type = "zookeeper";
        String namespace = "orchestration";
        OrchestrationRepositoryConfiguration configuration = new OrchestrationRepositoryConfiguration(type, new Properties());
        configuration.setNamespace(namespace);
        assertThat(configuration.getNamespace(), is(namespace));
    }
}
