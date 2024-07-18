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

package org.apache.shardingsphere.mode.manager.cluster.event.builder;

import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.dispatch.assisted.DropDatabaseListenerAssistedEvent;
import org.apache.shardingsphere.mode.event.dispatch.assisted.CreateDatabaseListenerAssistedEvent;
import org.apache.shardingsphere.mode.persist.pojo.ListenerAssistedType;
import org.apache.shardingsphere.mode.persist.pojo.ListenerAssisted;
import org.apache.shardingsphere.mode.path.ListenerAssistedNodePath;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Listener assisted dispatch event builder.
 */
public final class ListenerAssistedDispatchEventBuilder implements DispatchEventBuilder<DispatchEvent> {
    
    @Override
    public Collection<String> getSubscribedKeys() {
        return Collections.singleton(ListenerAssistedNodePath.getRootNodePath());
    }
    
    @Override
    public Collection<DataChangedEvent.Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED);
    }
    
    @Override
    public Optional<DispatchEvent> build(final DataChangedEvent event) {
        Optional<String> databaseName = ListenerAssistedNodePath.getDatabaseName(event.getKey());
        if (!databaseName.isPresent()) {
            return Optional.empty();
        }
        ListenerAssisted data = YamlEngine.unmarshal(event.getValue(), ListenerAssisted.class);
        if (ListenerAssistedType.CREATE_DATABASE == data.getListenerAssistedType()) {
            return Optional.of(new CreateDatabaseListenerAssistedEvent(databaseName.get()));
        }
        return ListenerAssistedType.DROP_DATABASE == data.getListenerAssistedType()
                ? Optional.of(new DropDatabaseListenerAssistedEvent(databaseName.get()))
                : Optional.empty();
    }
}
