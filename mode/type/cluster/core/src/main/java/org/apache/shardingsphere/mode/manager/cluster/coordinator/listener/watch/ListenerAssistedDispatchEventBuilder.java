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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.listener.watch;

import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.builder.dispatch.DispatchEventBuilder;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.listener.DropDatabaseListenerAssistedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.listener.CreateDatabaseListenerAssistedEvent;
import org.apache.shardingsphere.mode.persist.pojo.ListenerAssistedType;
import org.apache.shardingsphere.mode.persist.pojo.ListenerAssistedPOJO;
import org.apache.shardingsphere.mode.path.ListenerAssistedNodePath;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Listener assisted dispatch event builder.
 */
public class ListenerAssistedDispatchEventBuilder implements DispatchEventBuilder<GovernanceEvent> {
    
    @Override
    public Collection<String> getSubscribedKeys() {
        return Collections.singleton(ListenerAssistedNodePath.getRootNodePath());
    }
    
    @Override
    public Collection<DataChangedEvent.Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED);
    }
    
    @Override
    public Optional<GovernanceEvent> build(final DataChangedEvent event) {
        Optional<String> databaseName = ListenerAssistedNodePath.getDatabaseName(event.getKey());
        if (!databaseName.isPresent()) {
            return Optional.empty();
        }
        ListenerAssistedPOJO data = YamlEngine.unmarshal(event.getValue(), ListenerAssistedPOJO.class);
        if (ListenerAssistedType.CREATE_DATABASE == data.getListenerAssistedType()) {
            return Optional.of(new CreateDatabaseListenerAssistedEvent(databaseName.get()));
        }
        return ListenerAssistedType.DROP_DATABASE == data.getListenerAssistedType()
                ? Optional.of(new DropDatabaseListenerAssistedEvent(databaseName.get()))
                : Optional.empty();
    }
}
