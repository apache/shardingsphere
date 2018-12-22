/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.internal.registry.config.listener;

import io.shardingsphere.orchestration.internal.registry.config.event.ConfigMapChangedEvent;
import io.shardingsphere.orchestration.internal.registry.config.node.ConfigurationNode;
import io.shardingsphere.orchestration.internal.registry.listener.PostShardingOrchestrationEventListener;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent.ChangedType;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Schema changed listener.
 *
 * @author panjuan
 */
public final class SchemaChangedListener extends PostShardingOrchestrationEventListener {
    
    private final Collection<String> currentSchemas = new LinkedList<>();
    
    private final Collection<String> tmpSchemas = new LinkedList<>();
    
    public SchemaChangedListener(final String name, final RegistryCenter regCenter) {
        super(regCenter, new ConfigurationNode(name).getSchemaPath());
    }
    
    @Override
    protected ConfigMapChangedEvent createShardingOrchestrationEvent(final DataChangedEvent event) {
        System.out.println(event.getKey());
        System.out.println(event.getValue());
        if (ChangedType.DELETED == event.getChangedType()) {
            event.getKey();
        }
        return new ConfigMapChangedEvent(Collections.EMPTY_MAP);
    }
}
