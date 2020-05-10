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

package org.apache.shardingsphere.orchestration.core.registrycenter.listener;

import org.apache.shardingsphere.orchestration.center.RegistryCenterRepository;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent.ChangedType;
import org.apache.shardingsphere.orchestration.core.common.listener.PostShardingCenterRepositoryEventListener;
import org.apache.shardingsphere.orchestration.core.registrycenter.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registrycenter.RegistryCenterNode;
import org.apache.shardingsphere.orchestration.core.registrycenter.RegistryCenterNodeStatus;
import org.apache.shardingsphere.orchestration.core.registrycenter.schema.OrchestrationSchema;

import java.util.Collections;

/**
 * Data source state changed listener.
 */
public final class DataSourceStateChangedListener extends PostShardingCenterRepositoryEventListener {
    
    private final RegistryCenterNode registryCenterNode;
    
    public DataSourceStateChangedListener(final String name, final RegistryCenterRepository registryCenterRepository) {
        super(registryCenterRepository, Collections.singleton(new RegistryCenterNode(name).getDataSourcesNodeFullRootPath()));
        registryCenterNode = new RegistryCenterNode(name);
    }
    
    @Override
    protected DisabledStateChangedEvent createShardingOrchestrationEvent(final DataChangedEvent event) {
        return new DisabledStateChangedEvent(getShardingSchema(event.getKey()), isDataSourceDisabled(event));
    }
    
    private OrchestrationSchema getShardingSchema(final String dataSourceNodeFullPath) {
        return registryCenterNode.getOrchestrationShardingSchema(dataSourceNodeFullPath);
    }
    
    private boolean isDataSourceDisabled(final DataChangedEvent event) {
        return RegistryCenterNodeStatus.DISABLED.toString().equalsIgnoreCase(event.getValue()) && ChangedType.UPDATED == event.getChangedType();
    }
}
