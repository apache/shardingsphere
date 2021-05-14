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

import org.apache.commons.collections4.SetUtils;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNode;
import org.apache.shardingsphere.governance.core.registry.listener.PostGovernanceRepositoryEventListener;
import org.apache.shardingsphere.governance.core.registry.listener.event.GovernanceEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.metadata.MetaDataDeletedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.metadata.MetaDataPersistedEvent;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
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
public final class MetaDataChangedListener extends PostGovernanceRepositoryEventListener<GovernanceEvent> {
    
    private final Collection<String> existedSchemaNames;
    
    private final RegistryCenterNode registryCenterNode;
    
    public MetaDataChangedListener(final RegistryCenterRepository registryCenterRepository, final Collection<String> schemaNames) {
        super(registryCenterRepository, Collections.singleton(new RegistryCenterNode().getMetadataNodePath()));
        registryCenterNode = new RegistryCenterNode();
        existedSchemaNames = new LinkedHashSet<>(schemaNames);
    }
    
    @Override
    protected Optional<GovernanceEvent> createEvent(final DataChangedEvent event) {
        if (registryCenterNode.getMetadataNodePath().equals(event.getKey())) {
            Collection<String> persistedSchemaNames = registryCenterNode.splitSchemaName(event.getValue());
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
        return new MetaDataPersistedEvent(schemaName);
    }
    
    private GovernanceEvent createDeletedEvent(final String schemaName) {
        existedSchemaNames.remove(schemaName);
        return new MetaDataDeletedEvent(schemaName);
    }
}
