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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.yaml;

import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.mode.event.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.event.storage.StorageNodeRole;

/**
 * YAML storage node data source swapper.
 */
public final class YamlStorageNodeDataSourceSwapper implements YamlConfigurationSwapper<YamlStorageNodeDataSource, StorageNodeDataSource> {
    
    @Override
    public YamlStorageNodeDataSource swapToYamlConfiguration(final StorageNodeDataSource data) {
        YamlStorageNodeDataSource result = new YamlStorageNodeDataSource();
        result.setRole(data.getRole().name());
        result.setStatus(data.getStatus().name());
        return result;
    }
    
    @Override
    public StorageNodeDataSource swapToObject(final YamlStorageNodeDataSource yamlConfig) {
        return new StorageNodeDataSource(StorageNodeRole.valueOf(yamlConfig.getRole()), DataSourceState.valueOf(yamlConfig.getStatus()));
    }
}
