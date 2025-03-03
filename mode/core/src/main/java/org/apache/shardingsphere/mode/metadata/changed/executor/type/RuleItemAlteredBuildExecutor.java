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

package org.apache.shardingsphere.mode.metadata.changed.executor.type;

import org.apache.shardingsphere.mode.metadata.changed.executor.RuleItemChangedBuildExecutor;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePathParser;
import org.apache.shardingsphere.mode.node.rule.node.DatabaseRuleNode;
import org.apache.shardingsphere.mode.spi.rule.item.RuleChangedItemType;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterNamedRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterUniqueRuleItem;

import java.util.Optional;

/**
 * Rule item altered build executor.
 */
public final class RuleItemAlteredBuildExecutor implements RuleItemChangedBuildExecutor<AlterRuleItem> {
    
    @Override
    public Optional<AlterRuleItem> build(final DatabaseRuleNode databaseRuleNode, final String databaseName, final String path, final Integer activeVersion) {
        for (String each : databaseRuleNode.getNamedItems()) {
            DatabaseRuleNodePath databaseRuleNodePath = new DatabaseRuleNodePath(databaseName, databaseRuleNode.getRuleType(), new DatabaseRuleItem(each, NodePathPattern.QUALIFIED_IDENTIFIER));
            Optional<String> itemName = new VersionNodePathParser(databaseRuleNodePath).findIdentifierByActiveVersionPath(path, 1);
            if (itemName.isPresent()) {
                return Optional.of(new AlterNamedRuleItem(databaseName, itemName.get(), activeVersion, new RuleChangedItemType(databaseRuleNode.getRuleType(), each)));
            }
        }
        for (String each : databaseRuleNode.getUniqueItems()) {
            DatabaseRuleNodePath databaseRuleNodePath = new DatabaseRuleNodePath(databaseName, databaseRuleNode.getRuleType(), new DatabaseRuleItem(each));
            if (new VersionNodePathParser(databaseRuleNodePath).isActiveVersionPath(path)) {
                return Optional.of(new AlterUniqueRuleItem(databaseName, activeVersion, new RuleChangedItemType(databaseRuleNode.getRuleType(), each)));
            }
        }
        return Optional.empty();
    }
}
