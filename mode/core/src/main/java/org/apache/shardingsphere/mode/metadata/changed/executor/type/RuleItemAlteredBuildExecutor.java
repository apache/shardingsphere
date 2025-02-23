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
import org.apache.shardingsphere.mode.node.path.type.config.database.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.path.type.config.database.item.NamedDatabaseRuleItemNodePath;
import org.apache.shardingsphere.mode.node.path.type.config.database.item.UniqueDatabaseRuleItemNodePath;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterNamedRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterUniqueRuleItem;

import java.util.Map.Entry;
import java.util.Optional;

/**
 * Rule item altered build executor.
 */
public final class RuleItemAlteredBuildExecutor implements RuleItemChangedBuildExecutor<AlterRuleItem> {
    
    @Override
    public Optional<AlterRuleItem> build(final DatabaseRuleNodePath databaseRuleNodePath, final String databaseName, final String path, final Integer activeVersion) {
        for (Entry<String, NamedDatabaseRuleItemNodePath> entry : databaseRuleNodePath.getNamedItems().entrySet()) {
            Optional<String> itemName = entry.getValue().getVersionNodePathParser().findIdentifierByActiveVersionPath(path, 2);
            if (itemName.isPresent()) {
                return Optional.of(new AlterNamedRuleItem(databaseName, itemName.get(), path, activeVersion, databaseRuleNodePath.getRoot().getRuleType() + "." + entry.getKey()));
            }
        }
        for (Entry<String, UniqueDatabaseRuleItemNodePath> entry : databaseRuleNodePath.getUniqueItems().entrySet()) {
            if (entry.getValue().getVersionNodePathParser().isActiveVersionPath(path)) {
                return Optional.of(new AlterUniqueRuleItem(databaseName, path, activeVersion, databaseRuleNodePath.getRoot().getRuleType() + "." + entry.getKey()));
            }
        }
        return Optional.empty();
    }
}
