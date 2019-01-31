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

package org.apache.shardingsphere.orchestration.yaml.loader.impl;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.shardingsphere.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.orchestration.yaml.config.YamlDataSourceConfiguration;
import org.apache.shardingsphere.orchestration.yaml.loader.YamlLoader;
import org.apache.shardingsphere.orchestration.yaml.swapper.DataSourceConfigurationYamlSwapper;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

/**
 * Data source configurations YAML loader.
 *
 * @author panjuan
 * @author zhangliang
 */
public final class DataSourceConfigurationsYamlLoader implements YamlLoader<Map<String, DataSourceConfiguration>> {
    
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, DataSourceConfiguration> load(final String data) {
        Map<String, YamlDataSourceConfiguration> result = (Map) new Yaml().load(data);
        Preconditions.checkState(null != result && !result.isEmpty(), "No available data sources to load for orchestration.");
        return Maps.transformValues(result, new Function<YamlDataSourceConfiguration, DataSourceConfiguration>() {
            
            @Override
            public DataSourceConfiguration apply(final YamlDataSourceConfiguration input) {
                return new DataSourceConfigurationYamlSwapper().swap(input);
            }
        });
    }
}
