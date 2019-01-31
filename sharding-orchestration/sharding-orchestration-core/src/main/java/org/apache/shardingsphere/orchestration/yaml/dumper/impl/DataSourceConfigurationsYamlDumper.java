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

package org.apache.shardingsphere.orchestration.yaml.dumper.impl;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.shardingsphere.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.orchestration.yaml.config.YamlDataSourceConfiguration;
import org.apache.shardingsphere.orchestration.yaml.dumper.DefaultYamlRepresenter;
import org.apache.shardingsphere.orchestration.yaml.dumper.YamlDumper;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

/**
 * Data source configurations YAML dumper.
 *
 * @author panjuan
 * @author zhangliang
 */
public final class DataSourceConfigurationsYamlDumper implements YamlDumper<Map<String, DataSourceConfiguration>> {
    
    @Override
    public String dump(final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        return new Yaml(new DefaultYamlRepresenter()).dumpAsMap(
                Maps.transformValues(dataSourceConfigurations, new Function<DataSourceConfiguration, YamlDataSourceConfiguration>() {
                
                    @Override
                    public YamlDataSourceConfiguration apply(final DataSourceConfiguration input) {
                        YamlDataSourceConfiguration result = new YamlDataSourceConfiguration();
                        result.setDataSourceClassName(input.getDataSourceClassName());
                        result.setProperties(input.getProperties());
                        return result;
                    }
                }));
    }
}
