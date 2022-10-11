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

import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlPersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.YamlPersistRepositoryConfigurationSwapper;

/**
 * Cluster YAML persist repository configuration swapper.
 */
public final class ClusterYamlPersistRepositoryConfigurationSwapper implements YamlPersistRepositoryConfigurationSwapper<ClusterPersistRepositoryConfiguration> {
    
    @Override
    public YamlPersistRepositoryConfiguration swapToYamlConfiguration(final ClusterPersistRepositoryConfiguration data) {
        YamlPersistRepositoryConfiguration result = new YamlPersistRepositoryConfiguration();
        result.setType(data.getType());
        result.setProps(data.getProps());
        result.getProps().setProperty("namespace", data.getNamespace());
        result.getProps().setProperty("server-lists", data.getServerLists());
        return result;
    }
    
    @Override
    public ClusterPersistRepositoryConfiguration swapToObject(final YamlPersistRepositoryConfiguration yamlConfig) {
        return new ClusterPersistRepositoryConfiguration(
                yamlConfig.getType(), yamlConfig.getProps().getProperty("namespace"), yamlConfig.getProps().getProperty("server-lists"), yamlConfig.getProps());
    }
    
    @Override
    public String getType() {
        return "Cluster";
    }
}
