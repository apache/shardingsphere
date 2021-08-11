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

import org.apache.shardingsphere.infra.mode.config.ModeConfiguration;
import org.apache.shardingsphere.infra.mode.config.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlModeConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlConfigurationSwapper;

/**
 * Mode rule configuration YAML swapper.
 */
public final class ModeConfigurationYamlSwapper implements YamlConfigurationSwapper<YamlModeConfiguration, ModeConfiguration> {
    
    @Override
    public YamlModeConfiguration swapToYamlConfiguration(final ModeConfiguration data) {
        YamlModeConfiguration result = new YamlModeConfiguration();
        result.setType(data.getType());
        if (null != data.getRepository()) {
            result.setRepository(new PersistRepositoryConfigurationYamlSwapperEngine().swapToYamlConfiguration(data.getType(), data.getRepository()));
        }
        result.setOverwrite(data.isOverwrite());
        return result;
    }
    
    @Override
    public ModeConfiguration swapToObject(final YamlModeConfiguration yamlConfig) {
        PersistRepositoryConfiguration repositoryConfig = null == yamlConfig.getRepository()
                ? null : new PersistRepositoryConfigurationYamlSwapperEngine().swapToObject(yamlConfig.getType(), yamlConfig.getRepository());
        return new ModeConfiguration(yamlConfig.getType(), repositoryConfig, yamlConfig.isOverwrite());
    }
}
