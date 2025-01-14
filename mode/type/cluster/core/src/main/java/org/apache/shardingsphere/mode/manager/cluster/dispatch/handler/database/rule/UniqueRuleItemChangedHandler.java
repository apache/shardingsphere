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
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.node.path.rule.RuleNodePath;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterUniqueRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.drop.DropUniqueRuleItem;

import java.sql.SQLException;

/**
 * Unique rule item changed handler.
 */
@RequiredArgsConstructor
public final class UniqueRuleItemChangedHandler {
    
    private final ContextManager contextManager;
    
    /**
     * Handle unique rule item changed.
     *
     * @param ruleNodePath rule node path
     * @param databaseName database name
     * @param path path
     * @param event data changed event
     * @throws SQLException SQL Exception
     */
    public void handle(final RuleNodePath ruleNodePath, final String databaseName, final String path, final DataChangedEvent event) throws SQLException {
        String type = ruleNodePath.getRoot().getRuleType() + "." + path;
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            contextManager.getMetaDataContextManager().getRuleItemManager().alterRuleItem(new AlterUniqueRuleItem(databaseName, event.getKey(), event.getValue(), type));
        } else if (Type.DELETED == event.getType()) {
            contextManager.getMetaDataContextManager().getRuleItemManager().dropRuleItem(new DropUniqueRuleItem(databaseName, type));
        }
    }
}
