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

import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateBroadcastTableRuleStatement;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Create broadcast table rule statement updater.
 */
public final class CreateBroadcastTableRuleStatementUpdater implements RuleDefinitionCreateUpdater<CreateBroadcastTableRuleStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateBroadcastTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (!sqlStatement.isIfNotExists()) {
            checkDuplicate(sqlStatement, currentRuleConfig);
        }
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final CreateBroadcastTableRuleStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        Collection<String> tables = sqlStatement.getTables();
        if (sqlStatement.isIfNotExists()) {
            Collection<String> duplicatedRuleNames = getDuplicatedRuleNames(sqlStatement, currentRuleConfig);
            tables.removeIf(each -> duplicatedRuleNames.contains(each));
        }
        result.setBroadcastTables(tables);
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeCreatedRuleConfig) {
        currentRuleConfig.getBroadcastTables().addAll(toBeCreatedRuleConfig.getBroadcastTables());
    }
    
    private Collection<String> getDuplicatedRuleNames(final CreateBroadcastTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        if (null != currentRuleConfig && !currentRuleConfig.getBroadcastTables().isEmpty()) {
            result.addAll(currentRuleConfig.getBroadcastTables());
        }
        result.retainAll(sqlStatement.getTables());
        return result;
    }
    
    private void checkDuplicate(final CreateBroadcastTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> duplicatedRuleNames = getDuplicatedRuleNames(sqlStatement, currentRuleConfig);
        ShardingSpherePreconditions.checkState(duplicatedRuleNames.isEmpty(), () -> new DuplicateRuleException("Broadcast", sqlStatement.getTables()));
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateBroadcastTableRuleStatement.class.getName();
    }
}
