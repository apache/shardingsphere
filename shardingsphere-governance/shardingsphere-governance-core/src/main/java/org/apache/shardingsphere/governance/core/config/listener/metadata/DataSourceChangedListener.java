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

package org.apache.shardingsphere.governance.core.config.listener.metadata;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.governance.core.config.ConfigCenterNode;
import org.apache.shardingsphere.governance.core.event.listener.PostGovernanceRepositoryEventListener;
import org.apache.shardingsphere.governance.core.event.model.GovernanceEvent;
import org.apache.shardingsphere.governance.core.event.model.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.governance.core.yaml.config.YamlDataSourceConfigurationWrap;
import org.apache.shardingsphere.governance.core.yaml.swapper.DataSourceConfigurationYamlSwapper;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Data source changed listener.
 */
public final class DataSourceChangedListener extends PostGovernanceRepositoryEventListener<GovernanceEvent> {
    
    private final ConfigCenterNode configurationNode;
    
    public DataSourceChangedListener(final ConfigurationRepository configurationRepository, final Collection<String> schemaNames) {
        super(configurationRepository, new ConfigCenterNode().getAllDataSourcePaths(schemaNames));
        configurationNode = new ConfigCenterNode();
    }
    
    @Override
    protected Optional<GovernanceEvent> createEvent(final DataChangedEvent event) {
        String schemaName = new ConfigCenterNode().getSchemaName(event.getKey());
        if (Strings.isNullOrEmpty(schemaName) || !isDataSourceChangedEvent(schemaName, event.getKey()) 
                || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        return Optional.of(createDataSourceChangedEvent(schemaName, event));
    }
    
    private boolean isDataSourceChangedEvent(final String schemaName, final String eventPath) {
        return configurationNode.getDataSourcePath(schemaName).equals(eventPath);
    }
    
    private DataSourceChangedEvent createDataSourceChangedEvent(final String schemaName, final DataChangedEvent event) {
        YamlDataSourceConfigurationWrap result = YamlEngine.unmarshal(event.getValue(), YamlDataSourceConfigurationWrap.class);
        Preconditions.checkState(null != result && !result.getDataSources().isEmpty(), "No available data sources to load for governance.");
        return new DataSourceChangedEvent(schemaName, result.getDataSources().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> new DataSourceConfigurationYamlSwapper().swapToObject(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
    }
}
