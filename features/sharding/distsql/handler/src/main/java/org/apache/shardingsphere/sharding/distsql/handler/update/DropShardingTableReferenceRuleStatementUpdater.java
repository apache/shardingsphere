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

package org.apache.shardingsphere.sharding.distsql.handler.update;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.TableReferenceRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingTableReferenceRuleStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop sharding table reference rule statement updater.
 */
public final class DropShardingTableReferenceRuleStatementUpdater implements RuleDefinitionDropUpdater<DropShardingTableReferenceRuleStatement, ShardingRuleConfiguration> {
    
    private Map<String, String> bindingTableRules = Collections.emptyMap();
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final DropShardingTableReferenceRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (!isExistRuleConfig(currentRuleConfig) && sqlStatement.isIfExists()) {
            return;
        }
        String databaseName = database.getName();
        checkCurrentRuleConfiguration(databaseName, currentRuleConfig);
        bindingTableRules = buildBindingTableRule(currentRuleConfig);
        checkBindingTableRuleExist(databaseName, sqlStatement, bindingTableRules);
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final ShardingRuleConfiguration currentRuleConfig) {
        ShardingSpherePreconditions.checkState(null != currentRuleConfig && !currentRuleConfig.getBindingTableGroups().isEmpty(),
                () -> new MissingRequiredRuleException("Sharding table reference", databaseName));
    }
    
    private Map<String, String> buildBindingTableRule(final ShardingRuleConfiguration config) {
        Map<String, String> result = new LinkedHashMap<>();
        config.getBindingTableGroups().forEach(each -> Arrays.stream(each.split(",")).forEach(each1 -> result.put(each1, each)));
        return result;
    }
    
    private void checkBindingTableRuleExist(final String databaseName, final DropShardingTableReferenceRuleStatement sqlStatement,
                                            final Map<String, String> bindingRelationship) {
        if (sqlStatement.isIfExists()) {
            return;
        }
        Collection<String> notExistBindingGroups = new LinkedList<>();
        for (TableReferenceRuleSegment each : sqlStatement.getRules()) {
            if (!isToBeDroppedRuleExists(each, bindingRelationship)) {
                notExistBindingGroups.add(each.getTableGroup());
            }
        }
        ShardingSpherePreconditions.checkState(notExistBindingGroups.isEmpty(), () -> new MissingRequiredRuleException("Binding", databaseName, notExistBindingGroups));
    }
    
    private boolean isToBeDroppedRuleExists(final TableReferenceRuleSegment bindingRule, final Map<String, String> bindingRelationship) {
        Optional<String> anyTableInToBeAlteredRule = bindingRule.getTableReference().stream().findAny();
        if (anyTableInToBeAlteredRule.isPresent()) {
            Optional<String> currentBindingRule = bindingRelationship.entrySet().stream()
                    .filter(each -> each.getKey().equalsIgnoreCase(anyTableInToBeAlteredRule.get())).map(Entry::getValue).findFirst();
            if (currentBindingRule.isPresent() && !Strings.isNullOrEmpty(currentBindingRule.get())) {
                Collection<String> currentBindingTables = Splitter.on(",").trimResults().splitToList(currentBindingRule.get());
                return bindingRule.getTableReference().stream().allMatch(each -> containsIgnoreCase(currentBindingTables, each));
            }
        }
        return false;
    }
    
    private boolean containsIgnoreCase(final Collection<String> collection, final String str) {
        return collection.stream().anyMatch(each -> each.equalsIgnoreCase(str));
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShardingTableReferenceRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (!isExistRuleConfig(currentRuleConfig)) {
            return false;
        }
        if (bindingTableRules.isEmpty()) {
            bindingTableRules = buildBindingTableRule(currentRuleConfig);
        }
        return !getExistedBindingGroups(sqlStatement, bindingTableRules).isEmpty();
    }
    
    private Collection<String> getExistedBindingGroups(final DropShardingTableReferenceRuleStatement sqlStatement, final Map<String, String> bindingTableRules) {
        Collection<String> result = new LinkedList<>();
        if (sqlStatement.getRules().isEmpty()) {
            return new LinkedHashSet<>(bindingTableRules.values());
        }
        for (TableReferenceRuleSegment each : sqlStatement.getRules()) {
            if (isToBeDroppedRuleExists(each, bindingTableRules)) {
                result.add(each.getTableGroup());
            }
        }
        return result;
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropShardingTableReferenceRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getBindingTableGroups().clear();
        if (!sqlStatement.getRules().isEmpty()) {
            getToBeRemoveShardingTable(sqlStatement).forEach(each -> bindingTableRules.remove(each));
            currentRuleConfig.getBindingTableGroups().addAll(new LinkedHashSet<>(bindingTableRules.values()));
        }
        return false;
    }
    
    private Collection<String> getToBeRemoveShardingTable(final DropShardingTableReferenceRuleStatement sqlStatement) {
        Collection<String> toBeRemoveBindingTables = sqlStatement.getRules().stream().map(TableReferenceRuleSegment::getTableReference).flatMap(Collection::stream).collect(Collectors.toSet());
        return bindingTableRules.keySet().stream().filter(each -> containsIgnoreCase(toBeRemoveBindingTables, each)).collect(Collectors.toSet());
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropShardingTableReferenceRuleStatement.class.getName();
    }
}
