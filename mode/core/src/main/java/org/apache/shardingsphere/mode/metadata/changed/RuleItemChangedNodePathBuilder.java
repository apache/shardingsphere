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

package org.apache.shardingsphere.mode.metadata.changed;

import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;
import org.apache.shardingsphere.mode.node.rule.node.DatabaseRuleNode;
import org.apache.shardingsphere.mode.node.rule.node.DatabaseRuleNodeGenerator;

import java.util.Optional;

/**
 * Rule item changed node path builder.
 */
public final class RuleItemChangedNodePathBuilder {
    
    /**
     * Build rule item.
     *
     * @param databaseName database name
     * @param path path
     * @param eventType event type
     * @return built database rule node path
     */
    public Optional<DatabaseRuleNodePath> build(final String databaseName, final String path, final Type eventType) {
        Optional<String> ruleType = NodePathSearcher.find(path, DatabaseRuleNodePath.createRuleTypeSearchCriteria(databaseName));
        if (!ruleType.isPresent()) {
            return Optional.empty();
        }
        if (Type.DELETED == eventType && DatabaseRuleNodePath.isRuleTypePath(databaseName, path)) {
            return Optional.of(new DatabaseRuleNodePath(databaseName, ruleType.get(), null));
        }
        DatabaseRuleNode databaseRuleNode = DatabaseRuleNodeGenerator.generate(ruleType.get());
        for (String each : databaseRuleNode.getNamedItems()) {
            Optional<String> itemName = NodePathSearcher.find(path, DatabaseRuleNodePath.createRuleItemNameSearchCriteria(databaseName, databaseRuleNode.getRuleType(), each));
            if (!itemName.isPresent()) {
                continue;
            }
            DatabaseRuleNodePath databaseRuleNodePath = new DatabaseRuleNodePath(databaseName, databaseRuleNode.getRuleType(), new DatabaseRuleItem(each, itemName.get()));
            if (Type.DELETED == eventType) {
                return DatabaseRuleNodePath.isNamedRuleItemPath(databaseName, databaseRuleNode.getRuleType(), each, path) ? Optional.of(databaseRuleNodePath) : Optional.empty();
            }
            if (new VersionNodePath(databaseRuleNodePath).isActiveVersionPath(path)) {
                return Optional.of(databaseRuleNodePath);
            }
        }
        for (String each : databaseRuleNode.getUniqueItems()) {
            DatabaseRuleNodePath databaseRuleNodePath = new DatabaseRuleNodePath(databaseName, databaseRuleNode.getRuleType(), new DatabaseRuleItem(each));
            if (Type.DELETED == eventType) {
                return DatabaseRuleNodePath.isUniqueRuleItemPath(databaseName, databaseRuleNode.getRuleType(), each, path) ? Optional.of(databaseRuleNodePath) : Optional.empty();
            }
            if (new VersionNodePath(databaseRuleNodePath).isActiveVersionPath(path)) {
                return Optional.of(databaseRuleNodePath);
            }
        }
        return Optional.empty();
    }
}
