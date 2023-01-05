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
import org.apache.shardingsphere.distsql.handler.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateBroadcastTableRuleStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Create broadcast table rule statement updater.
 */
public final class CreateBroadcastTableRuleStatementUpdater implements RuleDefinitionCreateUpdater<CreateBroadcastTableRuleStatement, ShardingRuleConfiguration> {
    
    private boolean ifNotExists;
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateBroadcastTableRuleStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) throws RuleDefinitionViolationException {
        ifNotExists = sqlStatement.isIfNotExists();
        if (!ifNotExists) {
            checkDuplicate(sqlStatement, currentRuleConfig);
        }
    }
    
    private void checkDuplicate(final CreateBroadcastTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws RuleInUsedException {
        if (null == currentRuleConfig || currentRuleConfig.getBroadcastTables().isEmpty()) {
            return;
        }
        Collection<String> duplicateBroadcastTables = new LinkedList<>(currentRuleConfig.getBroadcastTables());
        duplicateBroadcastTables.retainAll(sqlStatement.getTables());
        ShardingSpherePreconditions.checkState(duplicateBroadcastTables.isEmpty(), () -> new DuplicateRuleException("Broadcast", sqlStatement.getTables()));
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final CreateBroadcastTableRuleStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setBroadcastTables(sqlStatement.getTables());
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeCreatedRuleConfig) {
        if (ifNotExists) {
            Collection<String> currentBroadCastTables = currentRuleConfig.getBroadcastTables();
            toBeCreatedRuleConfig.getBroadcastTables().removeIf(currentBroadCastTables::contains);
        }
        if (toBeCreatedRuleConfig.getBroadcastTables().isEmpty()) {
            return;
        }
        currentRuleConfig.getBroadcastTables().addAll(toBeCreatedRuleConfig.getBroadcastTables());
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
