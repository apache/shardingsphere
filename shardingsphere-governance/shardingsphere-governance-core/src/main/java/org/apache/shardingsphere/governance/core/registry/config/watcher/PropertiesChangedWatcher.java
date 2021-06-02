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

package org.apache.shardingsphere.governance.core.registry.config.watcher;

import org.apache.shardingsphere.governance.core.registry.config.node.GlobalNode;
import org.apache.shardingsphere.governance.core.registry.GovernanceWatcher;
import org.apache.shardingsphere.governance.core.registry.config.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

/**
 * Properties changed watcher.
 */
public final class PropertiesChangedWatcher implements GovernanceWatcher<PropertiesChangedEvent> {
    
    @Override
    public Collection<String> getWatchingKeys(final Collection<String> schemaNames) {
        return Collections.singleton(GlobalNode.getPropsPath());
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Collections.singleton(Type.UPDATED);
    }
    
    @Override
    public Optional<PropertiesChangedEvent> createGovernanceEvent(final DataChangedEvent event) {
        return Optional.of(new PropertiesChangedEvent(YamlEngine.unmarshal(event.getValue(), Properties.class)));
    }
}
