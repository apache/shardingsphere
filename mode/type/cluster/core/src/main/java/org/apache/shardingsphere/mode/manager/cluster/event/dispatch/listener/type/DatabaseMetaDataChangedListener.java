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

package org.apache.shardingsphere.mode.manager.cluster.event.dispatch.listener.type;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.mode.event.dispatch.builder.MetaDataChangedEventBuilder;
import org.apache.shardingsphere.mode.metadata.manager.RuleItemManager;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;

import java.util.Optional;

/**
 * Database meta data changed listener.
 */
@RequiredArgsConstructor
public final class DatabaseMetaDataChangedListener implements DataChangedEventListener {
    
    private final EventBusContext eventBusContext;
    
    private final RuleItemManager ruleItemManager;
    
    @Override
    public void onChange(final DataChangedEvent event) {
        handleDataChangedEvent(event);
    }
    
    private void handleDataChangedEvent(final DataChangedEvent event) {
        String key = event.getKey();
        Optional<String> databaseName = DatabaseMetaDataNode.getDatabaseNameBySchemaNode(key);
        if (!databaseName.isPresent()) {
            return;
        }
        Optional<DispatchEvent> metaDataChangedEvent = new MetaDataChangedEventBuilder().build(databaseName.get(), event);
        if (metaDataChangedEvent.isPresent()) {
            eventBusContext.post(metaDataChangedEvent.get());
        } else {
            ruleItemManager.alterRuleItem(databaseName.get(), event.getKey(), event.getValue(), event.getType());
        }
    }
}
