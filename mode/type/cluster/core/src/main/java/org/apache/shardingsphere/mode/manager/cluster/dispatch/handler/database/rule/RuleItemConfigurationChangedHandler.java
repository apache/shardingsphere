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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.rule;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.DatabaseLeafValueChangedHandler;
import org.apache.shardingsphere.mode.metadata.changed.RuleItemChangedNodePathBuilder;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleNodePath;

import java.util.Optional;

/**
 * Rule item configuration changed handler.
 */
@RequiredArgsConstructor
public abstract class RuleItemConfigurationChangedHandler implements DatabaseLeafValueChangedHandler {
    
    private final ContextManager contextManager;
    
    private final RuleItemChangedNodePathBuilder ruleItemChangedNodePathBuilder = new RuleItemChangedNodePathBuilder();
    
    @Override
    public final void handle(final String databaseName, final DataChangedEvent event) {
        Optional<DatabaseRuleNodePath> databaseRuleNodePath = ruleItemChangedNodePathBuilder.build(databaseName, event.getKey(), event.getType());
        if (!databaseRuleNodePath.isPresent()) {
            return;
        }
        switch (event.getType()) {
            case ADDED:
            case UPDATED:
                contextManager.getMetaDataContextManager().getDatabaseRuleItemManager().alter(databaseRuleNodePath.get());
                break;
            case DELETED:
                contextManager.getMetaDataContextManager().getDatabaseRuleItemManager().drop(databaseRuleNodePath.get());
                break;
            default:
                break;
        }
    }
}
