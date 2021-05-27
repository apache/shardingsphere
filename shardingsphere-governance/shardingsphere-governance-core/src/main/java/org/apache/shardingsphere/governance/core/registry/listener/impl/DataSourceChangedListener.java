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

package org.apache.shardingsphere.governance.core.registry.listener.impl;

import com.google.common.base.Strings;
import org.apache.shardingsphere.governance.core.registry.listener.PostGovernanceRepositoryEventListener;
import org.apache.shardingsphere.governance.core.registry.listener.event.GovernanceEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.governance.core.registry.service.config.node.SchemaMetadataNode;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlDataSourceConfigurationSwapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Data source changed listener.
 */
public final class DataSourceChangedListener extends PostGovernanceRepositoryEventListener<GovernanceEvent> {
    
    public DataSourceChangedListener(final RegistryCenterRepository registryCenterRepository, final Collection<String> schemaNames) {
        super(registryCenterRepository, SchemaMetadataNode.getAllDataSourcePaths(schemaNames));
    }
    
    @Override
    protected Optional<GovernanceEvent> createEvent(final DataChangedEvent event) {
        String schemaName = SchemaMetadataNode.getSchemaName(event.getKey());
        if (Strings.isNullOrEmpty(schemaName) || !isDataSourceChangedEvent(schemaName, event.getKey()) 
                || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        return Optional.of(createDataSourceChangedEvent(schemaName, event));
    }
    
    private boolean isDataSourceChangedEvent(final String schemaName, final String eventPath) {
        return SchemaMetadataNode.getMetadataDataSourcePath(schemaName).equals(eventPath);
    }
    
    @SuppressWarnings("unchecked")
    private DataSourceChangedEvent createDataSourceChangedEvent(final String schemaName, final DataChangedEvent event) {
        Map<String, Map<String, Object>> yamlDataSources = YamlEngine.unmarshal(event.getValue(), Map.class);
        return yamlDataSources.isEmpty() ? new DataSourceChangedEvent(schemaName, new HashMap<>())
                : new DataSourceChangedEvent(schemaName, yamlDataSources.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> new YamlDataSourceConfigurationSwapper()
                        .swapToDataSourceConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
    }
}
