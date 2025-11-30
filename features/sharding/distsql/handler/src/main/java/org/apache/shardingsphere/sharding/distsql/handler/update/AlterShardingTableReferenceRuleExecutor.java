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

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleAlterExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.checker.ShardingTableRuleStatementChecker;
import org.apache.shardingsphere.sharding.distsql.segment.table.TableReferenceRuleSegment;
import org.apache.shardingsphere.sharding.distsql.statement.AlterShardingTableReferenceRuleStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Alter sharding table reference rule executor.
 */
@DistSQLExecutorCurrentRuleRequired(ShardingRule.class)
@Setter
public final class AlterShardingTableReferenceRuleExecutor implements DatabaseRuleAlterExecutor<AlterShardingTableReferenceRuleStatement, ShardingRule, ShardingRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShardingRule rule;
    
    @Override
    public void checkBeforeUpdate(final AlterShardingTableReferenceRuleStatement sqlStatement) {
        checkToBeAlteredRulesExisted(sqlStatement);
        checkDuplicatedTablesInShardingTableReferenceRules(sqlStatement);
        checkToBeReferencedShardingTablesExisted(sqlStatement);
        checkShardingTableReferenceRulesValid(sqlStatement);
    }
    
    private void checkToBeAlteredRulesExisted(final AlterShardingTableReferenceRuleStatement sqlStatement) {
        Collection<String> currentRuleNames = rule.getConfiguration().getBindingTableGroups().stream().map(ShardingTableReferenceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> notExistedRuleNames = sqlStatement.getRules().stream().map(TableReferenceRuleSegment::getName).filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkMustEmpty(notExistedRuleNames, () -> new MissingRequiredRuleException("Sharding table reference", database.getName(), notExistedRuleNames));
    }
    
    private void checkDuplicatedTablesInShardingTableReferenceRules(final AlterShardingTableReferenceRuleStatement sqlStatement) {
        Collection<String> currentReferencedTableNames = getReferencedTableNames(getToBeAlteredRuleNames(sqlStatement));
        Collection<String> duplicatedTableNames = sqlStatement.getTableNames().stream().filter(currentReferencedTableNames::contains).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkMustEmpty(duplicatedTableNames, () -> new DuplicateRuleException("sharding table reference", database.getName(), duplicatedTableNames));
    }
    
    private Collection<String> getReferencedTableNames(final Collection<String> getToBeAlteredRuleNames) {
        Collection<String> result = new HashSet<>();
        rule.getConfiguration().getBindingTableGroups().forEach(each -> {
            if (!getToBeAlteredRuleNames.contains(each.getName())) {
                result.addAll(Arrays.stream(each.getReference().split(",")).map(String::trim).collect(Collectors.toSet()));
            }
        });
        return result;
    }
    
    private Collection<String> getToBeAlteredRuleNames(final AlterShardingTableReferenceRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().map(TableReferenceRuleSegment::getName).collect(Collectors.toSet());
    }
    
    private void checkToBeReferencedShardingTablesExisted(final AlterShardingTableReferenceRuleStatement sqlStatement) {
        Collection<String> currentShardingTableNames = getCurrentShardingTableNames();
        Collection<String> notExistedTableNames = sqlStatement.getTableNames().stream().filter(each -> !currentShardingTableNames.contains(each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkMustEmpty(notExistedTableNames, () -> new MissingRequiredRuleException("Sharding", database.getName(), notExistedTableNames));
    }
    
    private Collection<String> getCurrentShardingTableNames() {
        Collection<String> result = new CaseInsensitiveSet<>();
        result.addAll(rule.getConfiguration().getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        result.addAll(rule.getConfiguration().getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        return result;
    }
    
    private void checkShardingTableReferenceRulesValid(final AlterShardingTableReferenceRuleStatement sqlStatement) {
        Collection<ShardingTableReferenceRuleConfiguration> toBeAlteredShardingTableReferenceRules = buildToBeAlteredRuleConfiguration(sqlStatement).getBindingTableGroups();
        Collection<String> ruleNames = toBeAlteredShardingTableReferenceRules.stream().map(ShardingTableReferenceRuleConfiguration::getName).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(ShardingTableRuleStatementChecker.isValidBindingTableGroups(toBeAlteredShardingTableReferenceRules, rule.getConfiguration()),
                () -> new InvalidRuleConfigurationException("sharding table", ruleNames, Collections.singleton("invalid sharding table reference.")));
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterShardingTableReferenceRuleStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        sqlStatement.getRules().forEach(each -> result.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration(each.getName(), each.getReference())));
        return result;
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeDroppedRuleConfiguration(final ShardingRuleConfiguration toBeAlteredRuleConfig) {
        return null;
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<AlterShardingTableReferenceRuleStatement> getType() {
        return AlterShardingTableReferenceRuleStatement.class;
    }
}
