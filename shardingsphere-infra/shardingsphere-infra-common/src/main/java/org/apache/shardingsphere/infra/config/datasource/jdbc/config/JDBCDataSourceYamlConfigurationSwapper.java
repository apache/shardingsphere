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

package org.apache.shardingsphere.infra.config.datasource.jdbc.config;

import org.apache.shardingsphere.infra.yaml.config.swapper.YamlConfigurationSwapper;

/**
 * JDBC data source YAML configuration swapper.
 */
public final class JDBCDataSourceYamlConfigurationSwapper implements YamlConfigurationSwapper<YamlJDBCDataSourceConfiguration, JDBCDataSourceConfigurationWrapper> {
    
    @Override
    public YamlJDBCDataSourceConfiguration swapToYamlConfiguration(final JDBCDataSourceConfigurationWrapper data) {
        YamlJDBCDataSourceConfiguration result = new YamlJDBCDataSourceConfiguration();
        result.setType(data.getType());
        result.setParameter(data.getParameter());
        return result;
    }
    
    @Override
    public JDBCDataSourceConfigurationWrapper swapToObject(final YamlJDBCDataSourceConfiguration yamlConfig) {
        return new JDBCDataSourceConfigurationWrapper(yamlConfig.getType(), yamlConfig.getParameter());
    }
}
