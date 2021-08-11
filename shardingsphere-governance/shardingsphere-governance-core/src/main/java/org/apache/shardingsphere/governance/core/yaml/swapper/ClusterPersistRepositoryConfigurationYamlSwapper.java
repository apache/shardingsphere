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

package org.apache.shardingsphere.governance.core.yaml.swapper;

import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlPersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.PersistRepositoryConfigurationYamlSwapper;

/**
 * Cluster persist repository configuration YAML swapper.
 */
public final class ClusterPersistRepositoryConfigurationYamlSwapper implements PersistRepositoryConfigurationYamlSwapper<RegistryCenterConfiguration> {
    
    @Override
    public YamlPersistRepositoryConfiguration swapToYamlConfiguration(final RegistryCenterConfiguration data) {
        YamlPersistRepositoryConfiguration result = new YamlPersistRepositoryConfiguration();
        result.setType(data.getType());
        result.setProps(data.getProps());
        result.getProps().setProperty("namespace", data.getNamespace());
        result.getProps().setProperty("serverLists", data.getServerLists());
        return result;
    }
    
    @Override
    public RegistryCenterConfiguration swapToObject(final YamlPersistRepositoryConfiguration yamlConfig) {
        return new RegistryCenterConfiguration(yamlConfig.getType(), yamlConfig.getProps().getProperty("namespace"), yamlConfig.getProps().getProperty("serverLists"), yamlConfig.getProps());
    }
    
    @Override
    public String getType() {
        return "Cluster";
    }
}
