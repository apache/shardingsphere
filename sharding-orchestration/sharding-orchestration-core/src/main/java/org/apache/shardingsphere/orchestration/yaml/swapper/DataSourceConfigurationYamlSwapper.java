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

package org.apache.shardingsphere.orchestration.yaml.swapper;

import org.apache.shardingsphere.underlying.common.config.DataSourceConfiguration;
import org.apache.shardingsphere.underlying.common.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.orchestration.yaml.config.YamlDataSourceConfiguration;

/**
 * Data source configuration YAML swapper.
 *
 * @author zhangliang
 */
public final class DataSourceConfigurationYamlSwapper implements YamlSwapper<YamlDataSourceConfiguration, DataSourceConfiguration> {
    
    @Override
    public YamlDataSourceConfiguration swap(final DataSourceConfiguration data) {
        YamlDataSourceConfiguration result = new YamlDataSourceConfiguration();
        result.setDataSourceClassName(data.getDataSourceClassName());
        result.setProperties(data.getProperties());
        return result;
    }
    
    @Override
    public DataSourceConfiguration swap(final YamlDataSourceConfiguration yamlConfiguration) {
        DataSourceConfiguration result = new DataSourceConfiguration(yamlConfiguration.getDataSourceClassName());
        result.getProperties().putAll(yamlConfiguration.getProperties());
        return result;
    }
}
