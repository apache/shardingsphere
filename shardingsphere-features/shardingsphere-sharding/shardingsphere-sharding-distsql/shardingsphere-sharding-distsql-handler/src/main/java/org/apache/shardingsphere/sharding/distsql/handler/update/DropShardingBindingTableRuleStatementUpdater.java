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
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.segment.BindingTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingBindingTableRulesStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Drop sharding binding table rule statement updater.
 */
public final class DropShardingBindingTableRuleStatementUpdater implements RuleDefinitionDropUpdater<DropShardingBindingTableRulesStatement, ShardingRuleConfiguration> {
    
    private Map<String, String> bindingRelationship;
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final DropShardingBindingTableRulesStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        bindingRelationship = buildBindingRelationship(currentRuleConfig);
        checkBindingTableRuleExist(schemaName, sqlStatement, bindingRelationship);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        DistSQLException.predictionThrow(null != currentRuleConfig && !currentRuleConfig.getBindingTableGroups().isEmpty(), new RequiredRuleMissedException("Binding", schemaName));
    }
    
    private Map<String, String> buildBindingRelationship(final ShardingRuleConfiguration configuration) {
        Map<String, String> result = new LinkedHashMap<>();
        configuration.getBindingTableGroups().forEach(each -> Arrays.stream(each.split(",")).forEach(each1 -> result.put(each1, each)));
        return result;
    }
    
    private void checkBindingTableRuleExist(final String schemaName, final DropShardingBindingTableRulesStatement sqlStatement,
                                            final Map<String, String> bindingRelationship) throws DistSQLException {
        Collection<String> notExistBindingGroups = new LinkedList<>();
        for (BindingTableRuleSegment each : sqlStatement.getRules()) {
            if (!isToBeDroppedRuleExists(each, bindingRelationship)) {
                notExistBindingGroups.add(each.getTableGroups());
            }
        }
        DistSQLException.predictionThrow(notExistBindingGroups.isEmpty(), new RequiredRuleMissedException("Binding", schemaName, notExistBindingGroups));
    }
    
    private boolean isToBeDroppedRuleExists(final BindingTableRuleSegment bindingRule, final Map<String, String> bindingRelationship) {
        Optional<String> anyTableInToBeAlteredRule = bindingRule.getBindingTables().stream().findAny();
        if (anyTableInToBeAlteredRule.isPresent()) {
            String currentBindingRule = bindingRelationship.get(anyTableInToBeAlteredRule.get());
            if (!Strings.isNullOrEmpty(currentBindingRule)) {
                Collection<String> currentBindingTables = Splitter.on(",").trimResults().splitToList(currentBindingRule);
                return bindingRule.getBindingTables().containsAll(currentBindingTables);
            }
        }
        return false;
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropShardingBindingTableRulesStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getBindingTableGroups().clear();
        if (!sqlStatement.getRules().isEmpty()) {
            sqlStatement.getRules().forEach(each -> each.getBindingTables().forEach(each1 -> bindingRelationship.remove(each1)));
            currentRuleConfig.getBindingTableGroups().addAll(new LinkedHashSet<>(bindingRelationship.values()));
        }
        return false;
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropShardingBindingTableRulesStatement.class.getName();
    }
}
