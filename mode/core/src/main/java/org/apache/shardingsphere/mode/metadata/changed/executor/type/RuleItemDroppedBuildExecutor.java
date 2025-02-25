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
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePathParser;
import org.apache.shardingsphere.mode.node.spi.DatabaseRuleNode;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.spi.rule.item.drop.DropNamedRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.drop.DropRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.drop.DropUniqueRuleItem;

import java.util.Optional;

/**
 * Rule item dropped build executor.
 */
public final class RuleItemDroppedBuildExecutor implements RuleItemChangedBuildExecutor<DropRuleItem> {
    
    @Override
    public Optional<DropRuleItem> build(final DatabaseRuleNode databaseRuleNode, final String databaseName, final String path, final Integer activeVersion) {
        for (String each : databaseRuleNode.getNamedItems()) {
            Optional<String> itemName = NodePathSearcher.find(path, DatabaseRuleNodePath.createRuleItemNameSearchCriteria(databaseRuleNode.getRuleType(), each));
            if (itemName.isPresent()) {
                return Optional.of(new DropNamedRuleItem(databaseName, itemName.get(), databaseRuleNode.getRuleType() + "." + each));
            }
        }
        for (String each : databaseRuleNode.getUniqueItems()) {
            DatabaseRuleNodePath databaseRuleNodePath = new DatabaseRuleNodePath(NodePathPattern.IDENTIFIER, databaseRuleNode.getRuleType(), new DatabaseRuleItem(each));
            if (new VersionNodePathParser(databaseRuleNodePath).isActiveVersionPath(path)) {
                return Optional.of(new DropUniqueRuleItem(databaseName, databaseRuleNode.getRuleType() + "." + each));
            }
        }
        return Optional.empty();
    }
}
