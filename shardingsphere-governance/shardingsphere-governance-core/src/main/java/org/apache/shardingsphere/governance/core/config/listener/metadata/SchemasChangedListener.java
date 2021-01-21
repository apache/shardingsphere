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

import org.apache.commons.collections4.SetUtils;
import org.apache.shardingsphere.governance.core.config.ConfigCenterNode;
import org.apache.shardingsphere.governance.core.event.listener.PostGovernanceRepositoryEventListener;
import org.apache.shardingsphere.governance.core.event.model.GovernanceEvent;
import org.apache.shardingsphere.governance.core.event.model.metadata.MetaDataAddedEvent;
import org.apache.shardingsphere.governance.core.event.model.metadata.MetaDataDeletedEvent;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Schemas changed listener.
 */
public final class SchemasChangedListener extends PostGovernanceRepositoryEventListener<GovernanceEvent> {
    
    private final Collection<String> existedSchemaNames;
    
    private final ConfigCenterNode configurationNode;
    
    public SchemasChangedListener(final ConfigurationRepository configurationRepository, final Collection<String> schemaNames) {
        super(configurationRepository, Collections.singleton(new ConfigCenterNode().getMetadataNodePath()));
        configurationNode = new ConfigCenterNode();
        existedSchemaNames = new LinkedHashSet<>(schemaNames);
    }
    
    @Override
    protected Optional<GovernanceEvent> createEvent(final DataChangedEvent event) {
        if (configurationNode.getMetadataNodePath().equals(event.getKey())) {
            Collection<String> persistedSchemaNames = configurationNode.splitSchemaName(event.getValue());
            Set<String> addedSchemaNames = SetUtils.difference(new HashSet<>(persistedSchemaNames), new HashSet<>(existedSchemaNames));
            if (!addedSchemaNames.isEmpty()) {
                // TODO support multiple schemaNames
                return Optional.of(createAddedEvent(addedSchemaNames.iterator().next()));
            }
            Set<String> deletedSchemaNames = SetUtils.difference(new HashSet<>(existedSchemaNames), new HashSet<>(persistedSchemaNames));
            if (!deletedSchemaNames.isEmpty()) {
                String schemaName = deletedSchemaNames.iterator().next();
                // TODO support multiple schemaNames
                return Optional.of(createDeletedEvent(schemaName));
            }
        }
        return Optional.empty();
    }
    
    private GovernanceEvent createAddedEvent(final String schemaName) {
        existedSchemaNames.add(schemaName);
        return new MetaDataAddedEvent(schemaName, Collections.emptyMap(), Collections.emptyList());
    }
    
    private GovernanceEvent createDeletedEvent(final String schemaName) {
        existedSchemaNames.remove(schemaName);
        return new MetaDataDeletedEvent(schemaName);
    }
}
