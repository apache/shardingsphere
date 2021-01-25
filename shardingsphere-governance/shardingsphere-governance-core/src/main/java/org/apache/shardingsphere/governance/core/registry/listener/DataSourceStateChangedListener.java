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

package org.apache.shardingsphere.governance.core.registry.listener;

import org.apache.shardingsphere.governance.core.event.listener.PostGovernanceRepositoryEventListener;
import org.apache.shardingsphere.governance.core.event.model.GovernanceEvent;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNode;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNodeStatus;
import org.apache.shardingsphere.governance.core.registry.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.event.PrimaryStateChangedEvent;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;

import java.util.Collection;
import java.util.Optional;

/**
 * Data source state changed listener.
 */
public final class DataSourceStateChangedListener extends PostGovernanceRepositoryEventListener<GovernanceEvent> {
    
    private final RegistryCenterNode registryCenterNode;
    
    public DataSourceStateChangedListener(final RegistryRepository registryRepository, final Collection<String> schemaNames) {
        super(registryRepository, new RegistryCenterNode().getAllSchemaPaths(schemaNames));
        registryCenterNode = new RegistryCenterNode();
    }
    
    @Override
    protected Optional<GovernanceEvent> createEvent(final DataChangedEvent event) {
        if (registryCenterNode.isPrimaryDataSourcePath(event.getKey())) {
            return registryCenterNode.getPrimaryNodesGovernanceSchema(event.getKey()).map(schema -> new PrimaryStateChangedEvent(schema, event.getValue()));
        }
        return registryCenterNode.getGovernanceSchema(event.getKey()).map(schema -> new DisabledStateChangedEvent(schema, isDataSourceDisabled(event)));
    }
    
    private boolean isDataSourceDisabled(final DataChangedEvent event) {
        return RegistryCenterNodeStatus.DISABLED.toString().equalsIgnoreCase(event.getValue()) && (Type.UPDATED == event.getType() || Type.ADDED == event.getType());
    }
}
