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
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlPersistRepositoryConfiguration;

import java.util.Properties;

/**
 * Persist repository configuration YAML swapper engine.
 */
public final class PersistRepositoryConfigurationYamlSwapperEngine {
    
    static {
        ShardingSphereServiceLoader.register(PersistRepositoryConfigurationYamlSwapper.class);
    }
    
    /**
     * Swap to YAML configuration.
     * 
     * @param type persist repository type
     * @param config persist repository configuration
     * @return YAML persist repository configuration
     */
    @SuppressWarnings("unchecked")
    public YamlPersistRepositoryConfiguration swapToYamlConfiguration(final String type, final PersistRepositoryConfiguration config) {
        return (YamlPersistRepositoryConfiguration) TypedSPIRegistry.getRegisteredService(PersistRepositoryConfigurationYamlSwapper.class, type, new Properties()).swapToYamlConfiguration(config);
    }
    
    /**
     * Swap to YAML configuration.
     * 
     * @param type persist repository type
     * @param yamlConfig YAML persist repository configuration
     * @return persist repository configuration
     */
    @SuppressWarnings("unchecked")
    public PersistRepositoryConfiguration swapToObject(final String type, final YamlPersistRepositoryConfiguration yamlConfig) {
        return (PersistRepositoryConfiguration) TypedSPIRegistry.getRegisteredService(PersistRepositoryConfigurationYamlSwapper.class, type, new Properties()).swapToObject(yamlConfig);
    }
}
