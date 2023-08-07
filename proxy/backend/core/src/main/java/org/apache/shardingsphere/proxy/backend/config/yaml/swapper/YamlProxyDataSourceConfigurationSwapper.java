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

package org.apache.shardingsphere.proxy.backend.config.yaml.swapper;

import org.apache.shardingsphere.infra.datasource.config.ConnectionConfiguration;
import org.apache.shardingsphere.infra.datasource.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.config.PoolConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;

/**
 * YAML proxy data source configuration swapper.
 */
public final class YamlProxyDataSourceConfigurationSwapper {
    
    /**
     * Swap YAML proxy data source configuration to data source configuration.
     *
     * @param yamlConfig YAML proxy data source configuration
     * @return data source configuration
     */
    public DataSourceConfiguration swap(final YamlProxyDataSourceConfiguration yamlConfig) {
        return new DataSourceConfiguration(swapConnectionConfiguration(yamlConfig), swapPoolConfiguration(yamlConfig));
    }
    
    private ConnectionConfiguration swapConnectionConfiguration(final YamlProxyDataSourceConfiguration yamlConfig) {
        return new ConnectionConfiguration(yamlConfig.getDataSourceClassName(), yamlConfig.getUrl(), yamlConfig.getUsername(), yamlConfig.getPassword());
    }
    
    private PoolConfiguration swapPoolConfiguration(final YamlProxyDataSourceConfiguration yamlConfig) {
        return new PoolConfiguration(yamlConfig.getConnectionTimeoutMilliseconds(), yamlConfig.getIdleTimeoutMilliseconds(),
                yamlConfig.getMaxLifetimeMilliseconds(), yamlConfig.getMaxPoolSize(), yamlConfig.getMinPoolSize(), yamlConfig.getReadOnly(), yamlConfig.getCustomPoolProps());
    }
}
