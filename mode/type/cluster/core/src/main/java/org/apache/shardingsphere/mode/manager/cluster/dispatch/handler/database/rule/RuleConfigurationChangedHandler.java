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
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.DatabaseChangedHandler;
import org.apache.shardingsphere.mode.metadata.changed.RuleItemChangedNodePathBuilder;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePathParser;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Rule configuration changed handler.
 */
@RequiredArgsConstructor
public final class RuleConfigurationChangedHandler implements DatabaseChangedHandler {
    
    private final ContextManager contextManager;
    
    private final RuleItemChangedNodePathBuilder ruleItemChangedNodePathBuilder = new RuleItemChangedNodePathBuilder();
    
    @Override
    public boolean isSubscribed(final String databaseName, final String path) {
        Collection<DatabaseRuleNodePath> databaseRuleNodePaths = Arrays.asList(
                new DatabaseRuleNodePath(databaseName, NodePathPattern.QUALIFIED_IDENTIFIER, new DatabaseRuleItem(NodePathPattern.IDENTIFIER)),
                new DatabaseRuleNodePath(databaseName, NodePathPattern.IDENTIFIER, new DatabaseRuleItem(NodePathPattern.IDENTIFIER, NodePathPattern.QUALIFIED_IDENTIFIER)));
        return databaseRuleNodePaths.stream().anyMatch(each -> new VersionNodePathParser(each).isActiveVersionPath(path));
    }
    
    @Override
    public void handle(final String databaseName, final DataChangedEvent event) throws SQLException {
        Optional<DatabaseRuleNodePath> databaseRuleNodePath = ruleItemChangedNodePathBuilder.build(databaseName, event.getKey());
        if (!databaseRuleNodePath.isPresent()) {
            return;
        }
        switch (event.getType()) {
            case ADDED:
            case UPDATED:
                int version = Integer.parseInt(event.getValue());
                contextManager.getMetaDataContextManager().getDatabaseRuleItemManager().alter(databaseRuleNodePath.get(), version);
                break;
            case DELETED:
                contextManager.getMetaDataContextManager().getDatabaseRuleItemManager().drop(databaseRuleNodePath.get());
                break;
            default:
                break;
        }
    }
}
