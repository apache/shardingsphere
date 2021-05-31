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

import org.apache.shardingsphere.governance.core.registry.RegistryCenterNodeStatus;
import org.apache.shardingsphere.governance.core.registry.listener.GovernanceListener;
import org.apache.shardingsphere.governance.core.registry.listener.event.GovernanceEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.readwritesplitting.DisabledStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.readwritesplitting.PrimaryStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.service.state.StatesNode;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Data source state changed listener.
 */
public final class DataSourceStateChangedListener implements GovernanceListener<GovernanceEvent> {
    
    @Override
    public Collection<String> getWatchingKeys(final Collection<String> schemaNames) {
        return StatesNode.getAllSchemaPaths(schemaNames);
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED);
    }
    
    @Override
    public Optional<GovernanceEvent> createEvent(final DataChangedEvent event) {
        if (StatesNode.isPrimaryDataSourcePath(event.getKey())) {
            return StatesNode.getPrimaryNodesGovernanceSchema(event.getKey()).map(schema -> new PrimaryStateChangedEvent(schema, event.getValue()));
        }
        return StatesNode.getGovernanceSchema(event.getKey()).map(schema -> new DisabledStateChangedEvent(schema, isDataSourceDisabled(event)));
    }
    
    private boolean isDataSourceDisabled(final DataChangedEvent event) {
        return RegistryCenterNodeStatus.DISABLED.toString().equalsIgnoreCase(event.getValue()) && (Type.UPDATED == event.getType() || Type.ADDED == event.getType());
    }
}
