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

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.changed.RuleItemChangedBuildExecutor;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePath;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Rule configuration changed handler.
 */
@RequiredArgsConstructor
public final class RuleConfigurationChangedHandler {
    
    private final ContextManager contextManager;
    
    private final RuleItemChangedBuildExecutor ruleItemChangedBuildExecutor = new RuleItemChangedBuildExecutor();
    
    /**
     * Handle rule changed.
     *
     * @param databaseName database name
     * @param event data changed event
     * @throws SQLException SQL exception
     */
    public void handle(final String databaseName, final DataChangedEvent event) throws SQLException {
        if (Type.DELETED != event.getType() && Strings.isNullOrEmpty(event.getValue())) {
            return;
        }
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            if (!VersionNodePath.isActiveVersionPath(event.getKey())) {
                return;
            }
            int version = Integer.parseInt(event.getValue());
            Optional<DatabaseRuleNodePath> databaseRuleNodePath = ruleItemChangedBuildExecutor.build(databaseName, event.getKey());
            if (databaseRuleNodePath.isPresent()) {
                contextManager.getMetaDataContextManager().getDatabaseRuleItemManager().alter(databaseRuleNodePath.get(), version);
            }
        } else if (Type.DELETED == event.getType()) {
            Optional<DatabaseRuleNodePath> databaseRuleNodePath = ruleItemChangedBuildExecutor.build(databaseName, event.getKey());
            if (databaseRuleNodePath.isPresent()) {
                contextManager.getMetaDataContextManager().getDatabaseRuleItemManager().drop(databaseRuleNodePath.get());
            }
        }
    }
}
