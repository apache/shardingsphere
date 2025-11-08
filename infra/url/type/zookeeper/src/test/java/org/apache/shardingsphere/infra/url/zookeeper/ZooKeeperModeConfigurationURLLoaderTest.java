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

package org.apache.shardingsphere.infra.url.zookeeper;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.url.spi.ShardingSphereModeConfigurationURLLoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZooKeeperModeConfigurationURLLoaderTest {
    
    private final ShardingSphereModeConfigurationURLLoader urlLoader = TypedSPILoader.getService(ShardingSphereModeConfigurationURLLoader.class, "zookeeper:");
    
    @Test
    void assertLoadWithNamespace() {
        ModeConfiguration actual = urlLoader.load("localhost:2181,localhost:2182", PropertiesBuilder.build(new Property("namespace", "foo_namespace"), new Property("timeout", "3000")));
        assertThat(actual.getType(), is("Cluster"));
        ClusterPersistRepositoryConfiguration actualRepositoryConfig = (ClusterPersistRepositoryConfiguration) actual.getRepository();
        assertThat(actualRepositoryConfig.getType(), is("ZooKeeper"));
        assertThat(actualRepositoryConfig.getProps().getProperty("namespace"), is("foo_namespace"));
        assertThat(actualRepositoryConfig.getProps().getProperty("timeout"), is("3000"));
    }
    
    @Test
    void assertLoadWithoutNamespace() {
        RuntimeException expectedException = assertThrows(RuntimeException.class, () -> urlLoader.load("localhost:2181,localhost:2182", new Properties()));
        assertThat(expectedException.getMessage(), is("Missing required property 'namespace' for ZooKeeper URL loader."));
    }
}
