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

package org.apache.shardingsphere.mode.manager.cluster.yaml;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlPersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.YamlPersistRepositoryConfigurationSwapper;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings({"unchecked", "rawtypes"})
class ClusterYamlPersistRepositoryConfigurationSwapperTest {
    
    private final YamlPersistRepositoryConfigurationSwapper swapper = TypedSPILoader.getService(YamlPersistRepositoryConfigurationSwapper.class, "Cluster");
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlPersistRepositoryConfiguration actual = (YamlPersistRepositoryConfiguration) swapper.swapToYamlConfiguration(
                new ClusterPersistRepositoryConfiguration("TEST", "foo_namespace", "localhost", PropertiesBuilder.build(new Property("key", "value"))));
        assertThat(actual.getType(), is("TEST"));
        assertThat(actual.getProps().getProperty("key"), is("value"));
        assertThat(actual.getProps().getProperty("namespace"), is("foo_namespace"));
        assertThat(actual.getProps().getProperty("server-lists"), is("localhost"));
    }
    
    @Test
    void assertSwapToObject() {
        YamlPersistRepositoryConfiguration yamlConfig = new YamlPersistRepositoryConfiguration();
        yamlConfig.setType("TEST");
        yamlConfig.setProps(PropertiesBuilder.build(new Property("key", "value"), new Property("namespace", "foo_namespace"), new Property("server-lists", "localhost")));
        ClusterPersistRepositoryConfiguration actual = (ClusterPersistRepositoryConfiguration) swapper.swapToObject(yamlConfig);
        assertThat(actual.getType(), is("TEST"));
        assertThat(actual.getNamespace(), is("foo_namespace"));
        assertThat(actual.getServerLists(), is("localhost"));
        assertThat(actual.getProps().getProperty("key"), is("value"));
        assertThat(actual.getProps().getProperty("namespace"), is("foo_namespace"));
        assertThat(actual.getProps().getProperty("server-lists"), is("localhost"));
    }
}
