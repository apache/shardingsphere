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
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.node.path.rule.RuleNodePath;
import org.apache.shardingsphere.mode.node.path.rule.item.NamedRuleItemNodePath;
import org.apache.shardingsphere.mode.node.path.rule.item.UniqueRuleItemNodePath;
import org.apache.shardingsphere.mode.node.spi.RuleNodePathProvider;

import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Rule configuration changed handler.
 */
public final class RuleConfigurationChangedHandler {
    
    private final NamedRuleItemChangedHandler namedRuleItemChangedHandler;
    
    private final UniqueRuleItemChangedHandler uniqueRuleItemChangedHandler;
    
    public RuleConfigurationChangedHandler(final ContextManager contextManager) {
        namedRuleItemChangedHandler = new NamedRuleItemChangedHandler(contextManager);
        uniqueRuleItemChangedHandler = new UniqueRuleItemChangedHandler(contextManager);
    }
    
    /**
     * Handle rule changed.
     *
     * @param databaseName database name
     * @param event data changed event
     * @throws SQLException SQL Exception
     */
    public void handle(final String databaseName, final DataChangedEvent event) throws SQLException {
        for (RuleNodePathProvider each : ShardingSphereServiceLoader.getServiceInstances(RuleNodePathProvider.class)) {
            if (handle(each.getRuleNodePath(), databaseName, event)) {
                return;
            }
        }
    }
    
    private boolean handle(final RuleNodePath ruleNodePath, final String databaseName, final DataChangedEvent event) throws SQLException {
        if (!ruleNodePath.getRoot().isValidatedPath(event.getKey()) || Type.DELETED != event.getType() && Strings.isNullOrEmpty(event.getValue())) {
            return false;
        }
        return handleNamedRuleItems(ruleNodePath, databaseName, event) || handleUniqueRuleItems(ruleNodePath, databaseName, event);
    }
    
    private boolean handleNamedRuleItems(final RuleNodePath ruleNodePath, final String databaseName, final DataChangedEvent event) throws SQLException {
        for (Entry<String, NamedRuleItemNodePath> entry : ruleNodePath.getNamedItems().entrySet()) {
            Optional<String> itemName = getItemName(event, entry.getValue());
            if (itemName.isPresent()) {
                namedRuleItemChangedHandler.handle(ruleNodePath, databaseName, itemName.get(), entry.getKey(), event);
                return true;
            }
        }
        return false;
    }
    
    private boolean handleUniqueRuleItems(final RuleNodePath ruleNodePath, final String databaseName, final DataChangedEvent event) throws SQLException {
        for (Entry<String, UniqueRuleItemNodePath> entry : ruleNodePath.getUniqueItems().entrySet()) {
            if (entry.getValue().isActiveVersionPath(event.getKey())) {
                uniqueRuleItemChangedHandler.handle(ruleNodePath, databaseName, entry.getKey(), event);
                return true;
            }
        }
        return false;
    }
    
    private Optional<String> getItemName(final DataChangedEvent event, final NamedRuleItemNodePath ruleItemNodePath) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return ruleItemNodePath.getNameByActiveVersion(event.getKey());
        }
        if (Type.DELETED == event.getType()) {
            return ruleItemNodePath.getNameByItemPath(event.getKey());
        }
        return Optional.empty();
    }
}
