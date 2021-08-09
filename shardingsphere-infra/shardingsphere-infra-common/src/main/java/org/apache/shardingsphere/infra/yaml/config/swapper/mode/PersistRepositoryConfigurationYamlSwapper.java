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

package org.apache.shardingsphere.infra.yaml.config.swapper.mode;

import org.apache.shardingsphere.infra.mode.config.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlPersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlConfigurationSwapper;

/**
 * Persist repository configuration YAML swapper.
 */
public final class PersistRepositoryConfigurationYamlSwapper implements YamlConfigurationSwapper<YamlPersistRepositoryConfiguration, PersistRepositoryConfiguration> {
    
    @Override
    public YamlPersistRepositoryConfiguration swapToYamlConfiguration(final PersistRepositoryConfiguration data) {
        YamlPersistRepositoryConfiguration result = new YamlPersistRepositoryConfiguration();
        result.setType(data.getType());
        result.setProps(data.getProps());
        return result;
    }
    
    @Override
    public PersistRepositoryConfiguration swapToObject(final YamlPersistRepositoryConfiguration yamlConfig) {
        return new PersistRepositoryConfiguration(yamlConfig.getType(), yamlConfig.getProps());
    }
}
