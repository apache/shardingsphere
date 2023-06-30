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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.watcher;

import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.infra.config.nodepath.GlobalNodePath;
import org.apache.shardingsphere.metadata.persist.node.GlobalNode;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.config.global.AlterGlobalRuleConfigurationEvent;
import org.apache.shardingsphere.mode.event.config.global.DeleteGlobalRuleConfigurationEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.NewGovernanceWatcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * TODO Rename GlobalRuleChangedWatcher when metadata structure adjustment completed. #25485
 * Global rule changed watcher.
 */
public final class NewGlobalRuleChangedWatcher implements NewGovernanceWatcher<GovernanceEvent> {
    
    @Override
    public Collection<String> getWatchingKeys(final String databaseName) {
        return Collections.singleton(GlobalNode.getGlobalRuleNode());
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED);
    }
    
    @Override
    public Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
        return createGlobalRuleEvent(event);
    }
    
    private Optional<GovernanceEvent> createGlobalRuleEvent(final DataChangedEvent event) {
        if (GlobalNodePath.isRuleActiveVersionPath(event.getKey())) {
            Optional<String> ruleName = GlobalNodePath.getRuleName(event.getKey());
            if (!ruleName.isPresent()) {
                return Optional.empty();
            }
            if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
                return Optional.of(new AlterGlobalRuleConfigurationEvent(ruleName.get(), event.getKey(), event.getValue()));
            }
            return Optional.of(new DeleteGlobalRuleConfigurationEvent(ruleName.get()));
        }
        return Optional.empty();
    }
}
