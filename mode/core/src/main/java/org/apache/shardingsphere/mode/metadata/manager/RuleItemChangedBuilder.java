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

package org.apache.shardingsphere.mode.metadata.manager;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.node.path.rule.RuleNodePath;
import org.apache.shardingsphere.mode.node.path.rule.item.NamedRuleItemNodePath;
import org.apache.shardingsphere.mode.node.path.rule.item.UniqueRuleItemNodePath;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterNamedRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterUniqueRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.drop.DropNamedRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.drop.DropUniqueRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.RuleChangedItem;
import org.apache.shardingsphere.mode.node.spi.RuleNodePathProvider;

import java.util.Map.Entry;
import java.util.Optional;

/**
 * Rule item changed builder.
 */
public final class RuleItemChangedBuilder {
    
    /**
     * Build rule item changed.
     *
     * @param databaseName database name
     * @param activeVersionKey active version key
     * @param activeVersion active version
     * @param changedType data changed type
     * @return built rule item
     */
    public Optional<RuleChangedItem> build(final String databaseName, final String activeVersionKey, final String activeVersion, final Type changedType) {
        for (RuleNodePathProvider each : ShardingSphereServiceLoader.getServiceInstances(RuleNodePathProvider.class)) {
            Optional<RuleChangedItem> result = build(each.getRuleNodePath(), databaseName, activeVersionKey, activeVersion, changedType);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
    
    private Optional<RuleChangedItem> build(final RuleNodePath ruleNodePath, final String databaseName, final String activeVersionKey, final String activeVersion, final Type changedType) {
        if (!ruleNodePath.getRoot().isValidatedPath(activeVersionKey) || Type.DELETED != changedType && Strings.isNullOrEmpty(activeVersion)) {
            return Optional.empty();
        }
        for (Entry<String, NamedRuleItemNodePath> entry : ruleNodePath.getNamedItems().entrySet()) {
            Optional<String> itemName;
            if (Type.ADDED == changedType || Type.UPDATED == changedType) {
                itemName = entry.getValue().getNameByActiveVersion(activeVersionKey);
            } else {
                itemName = entry.getValue().getNameByItemPath(activeVersionKey);
            }
            if (itemName.isPresent()) {
                return Optional.of(create(databaseName, itemName.get(), activeVersionKey, activeVersion, changedType, ruleNodePath.getRoot().getRuleType() + "." + entry.getKey()));
            }
        }
        for (Entry<String, UniqueRuleItemNodePath> entry : ruleNodePath.getUniqueItems().entrySet()) {
            if (entry.getValue().isActiveVersionPath(activeVersionKey)) {
                return Optional.of(create(databaseName, activeVersionKey, activeVersion, changedType, ruleNodePath.getRoot().getRuleType() + "." + entry.getKey()));
            }
        }
        return Optional.empty();
    }
    
    private RuleChangedItem create(final String databaseName, final String itemName, final String activeVersionKey, final String activeVersion, final Type changedType, final String type) {
        return Type.ADDED == changedType || Type.UPDATED == changedType
                ? new AlterNamedRuleItem(databaseName, itemName, activeVersionKey, activeVersion, type)
                : new DropNamedRuleItem(databaseName, itemName, type);
    }
    
    private RuleChangedItem create(final String databaseName, final String activeVersionKey, final String activeVersion, final Type changedType, final String type) {
        return Type.ADDED == changedType || Type.UPDATED == changedType
                ? new AlterUniqueRuleItem(databaseName, activeVersionKey, activeVersion, type)
                : new DropUniqueRuleItem(databaseName, type);
    }
}
