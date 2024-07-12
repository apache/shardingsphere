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

package org.apache.shardingsphere.mode.event.builder;

import com.google.common.base.Strings;
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.dispatch.rule.RuleItemChangedEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.alter.AlterNamedRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.alter.AlterUniqueRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.drop.DropUniqueRuleItemEvent;
import org.apache.shardingsphere.mode.path.rule.RuleNodePath;
import org.apache.shardingsphere.mode.path.rule.item.NamedRuleItemNodePath;
import org.apache.shardingsphere.mode.path.rule.item.UniqueRuleItemNodePath;
import org.apache.shardingsphere.mode.spi.RuleNodePathProvider;

import java.util.Map.Entry;
import java.util.Optional;

/**
 * Rule configuration event builder.
 */
public final class RuleConfigurationEventBuilder {
    
    /**
     * Build rule changed event.
     *
     * @param databaseName database name
     * @param event data changed event
     * @return rule changed event
     */
    public Optional<DispatchEvent> build(final String databaseName, final DataChangedEvent event) {
        for (RuleNodePathProvider each : ShardingSphereServiceLoader.getServiceInstances(RuleNodePathProvider.class)) {
            Optional<DispatchEvent> result = build(each.getRuleNodePath(), databaseName, event);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
    
    private Optional<DispatchEvent> build(final RuleNodePath ruleNodePath, final String databaseName, final DataChangedEvent event) {
        if (!ruleNodePath.getRoot().isValidatedPath(event.getKey()) || Type.DELETED != event.getType() && Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        for (Entry<String, NamedRuleItemNodePath> entry : ruleNodePath.getNamedItems().entrySet()) {
            Optional<String> itemName = entry.getValue().getNameByActiveVersion(event.getKey());
            if (itemName.isPresent()) {
                return Optional.of(create(databaseName, itemName.get(), event, ruleNodePath.getRoot().getRuleType() + "." + entry.getKey()));
            }
        }
        for (Entry<String, UniqueRuleItemNodePath> entry : ruleNodePath.getUniqueItems().entrySet()) {
            if (entry.getValue().isActiveVersionPath(event.getKey())) {
                return Optional.of(create(databaseName, event, ruleNodePath.getRoot().getRuleType() + "." + entry.getKey()));
            }
        }
        return Optional.empty();
    }
    
    private RuleItemChangedEvent create(final String databaseName, final String itemName, final DataChangedEvent event, final String type) {
        return Type.ADDED == event.getType() || Type.UPDATED == event.getType()
                ? new AlterNamedRuleItemEvent(databaseName, itemName, event.getKey(), event.getValue(), type)
                : new DropNamedRuleItemEvent(databaseName, itemName, type);
    }
    
    private RuleItemChangedEvent create(final String databaseName, final DataChangedEvent event, final String type) {
        return Type.ADDED == event.getType() || Type.UPDATED == event.getType()
                ? new AlterUniqueRuleItemEvent(databaseName, event.getKey(), event.getValue(), type)
                : new DropUniqueRuleItemEvent(databaseName, type);
    }
}
