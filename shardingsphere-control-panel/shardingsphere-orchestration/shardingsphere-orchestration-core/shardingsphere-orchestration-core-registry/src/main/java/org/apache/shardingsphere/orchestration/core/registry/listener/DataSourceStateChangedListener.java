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

package org.apache.shardingsphere.orchestration.core.registry.listener;

import org.apache.shardingsphere.orchestration.core.registry.RegistryCenterNode;
import org.apache.shardingsphere.orchestration.core.registry.RegistryCenterNodeStatus;
import org.apache.shardingsphere.orchestration.core.registry.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registry.schema.OrchestrationSchema;
import org.apache.shardingsphere.orchestration.repository.api.RegistryRepository;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEvent.ChangedType;
import org.apache.shardingsphere.orchestration.core.common.listener.PostOrchestrationRepositoryEventListener;

import java.util.Collections;

/**
 * Data source state changed listener.
 */
public final class DataSourceStateChangedListener extends PostOrchestrationRepositoryEventListener {
    
    private final RegistryCenterNode registryCenterNode;
    
    public DataSourceStateChangedListener(final String name, final RegistryRepository registryRepository) {
        super(registryRepository, Collections.singleton(new RegistryCenterNode(name).getDataSourcesNodeFullRootPath()));
        registryCenterNode = new RegistryCenterNode(name);
    }
    
    @Override
    protected DisabledStateChangedEvent createOrchestrationEvent(final DataChangedEvent event) {
        return new DisabledStateChangedEvent(getShardingSchema(event.getKey()), isDataSourceDisabled(event));
    }
    
    private OrchestrationSchema getShardingSchema(final String dataSourceNodeFullPath) {
        return registryCenterNode.getOrchestrationShardingSchema(dataSourceNodeFullPath);
    }
    
    private boolean isDataSourceDisabled(final DataChangedEvent event) {
        return RegistryCenterNodeStatus.DISABLED.toString().equalsIgnoreCase(event.getValue())
                && (ChangedType.UPDATED == event.getChangedType() || ChangedType.ADDED == event.getChangedType());
    }
}
