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

package org.apache.shardingsphere.mode.storage.yaml;

import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.mode.storage.QualifiedDataSourceState;

/**
 * YAML qualified data source state swapper.
 */
public final class YamlQualifiedDataSourceStateSwapper implements YamlConfigurationSwapper<YamlQualifiedDataSourceState, QualifiedDataSourceState> {
    
    @Override
    public YamlQualifiedDataSourceState swapToYamlConfiguration(final QualifiedDataSourceState data) {
        YamlQualifiedDataSourceState result = new YamlQualifiedDataSourceState();
        result.setStatus(data.getState().name());
        return result;
    }
    
    @Override
    public QualifiedDataSourceState swapToObject(final YamlQualifiedDataSourceState yamlConfig) {
        return new QualifiedDataSourceState(DataSourceState.valueOf(yamlConfig.getStatus()));
    }
}
