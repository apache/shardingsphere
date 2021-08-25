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

package org.apache.shardingsphere.scaling.core.config.yaml;

import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.mode.config.ModeConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlModeConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlPersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.PersistRepositoryConfigurationYamlSwapper;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ServerConfigurationYamlSwapperTest {
    
    static {
        ShardingSphereServiceLoader.register(PersistRepositoryConfigurationYamlSwapper.class);
    }
    
    private final ServerConfigurationYamlSwapper serverConfigurationYamlSwapper = new ServerConfigurationYamlSwapper();
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlServerConfiguration yamlServerConfig = serverConfigurationYamlSwapper.swapToYamlConfiguration(mockServerConfig());
        assertThat(yamlServerConfig.getScaling().getWorkerThread(), is(10));
        assertThat(yamlServerConfig.getMode().getRepository().getProps().getProperty("namespace"), is("test"));
        assertThat(yamlServerConfig.getMode().getRepository().getType(), is("Zookeeper"));
    }
    
    @Test
    public void assertSwapToObject() {
        ServerConfiguration serverConfig = serverConfigurationYamlSwapper.swapToObject(mockYamlServerConfig());
        assertThat(serverConfig.getWorkerThread(), is(10));
        assertThat(serverConfig.getModeConfiguration().getRepository().getProps().getProperty("namespace"), is("test"));
    }
    
    private ServerConfiguration mockServerConfig() {
        ServerConfiguration result = new ServerConfiguration();
        result.setWorkerThread(10);
        result.setModeConfiguration(new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("Zookeeper", "test", "localhost:2181", new Properties()), false));
        return result;
    }
    
    private YamlServerConfiguration mockYamlServerConfig() {
        YamlModeConfiguration config = new YamlModeConfiguration();
        config.setType("Cluster");
        YamlPersistRepositoryConfiguration repositoryConfig = new YamlPersistRepositoryConfiguration();
        repositoryConfig.getProps().setProperty("namespace", "test");
        config.setRepository(repositoryConfig);
        YamlServerConfiguration result = new YamlServerConfiguration();
        result.setMode(config);
        result.getScaling().setWorkerThread(10);
        return result;
    }
}
