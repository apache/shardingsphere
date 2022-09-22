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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule;

import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterDefaultSingleTableRuleStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;

import java.util.Collections;
import java.util.Set;

/**
 * Alter default single table rule statement updater.
 */
public final class AlterDefaultSingleTableRuleStatementUpdater implements RuleDefinitionAlterUpdater<AlterDefaultSingleTableRuleStatement, SingleTableRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database,
                                  final AlterDefaultSingleTableRuleStatement sqlStatement, final SingleTableRuleConfiguration currentRuleConfig) throws DistSQLException {
        String databaseName = database.getName();
        checkConfigurationExist(databaseName, currentRuleConfig);
        checkResourceExist(database, sqlStatement);
        checkDefaultResourceExist(databaseName, currentRuleConfig);
    }
    
    private void checkConfigurationExist(final String databaseName, final SingleTableRuleConfiguration currentRuleConfig) throws DistSQLException {
        ShardingSpherePreconditions.checkNotNull(currentRuleConfig, () -> new MissingRequiredRuleException(databaseName, "single table"));
    }
    
    private void checkResourceExist(final ShardingSphereDatabase database, final AlterDefaultSingleTableRuleStatement sqlStatement) throws DistSQLException {
        Set<String> resourceNames = database.getResource().getDataSources().keySet();
        ShardingSpherePreconditions.checkState(resourceNames.contains(sqlStatement.getDefaultResource()),
                () -> new MissingRequiredResourcesException(database.getName(), Collections.singleton(sqlStatement.getDefaultResource())));
    }
    
    private void checkDefaultResourceExist(final String databaseName, final SingleTableRuleConfiguration currentRuleConfig) throws DistSQLException {
        ShardingSpherePreconditions.checkState(currentRuleConfig.getDefaultDataSource().isPresent(), () -> new MissingRequiredRuleException("single table", databaseName));
    }
    
    @Override
    public SingleTableRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterDefaultSingleTableRuleStatement sqlStatement) {
        SingleTableRuleConfiguration result = new SingleTableRuleConfiguration();
        result.setDefaultDataSource(sqlStatement.getDefaultResource());
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final SingleTableRuleConfiguration currentRuleConfig, final SingleTableRuleConfiguration toBeCreatedRuleConfig) {
        currentRuleConfig.setDefaultDataSource(toBeCreatedRuleConfig.getDefaultDataSource().get());
    }
    
    @Override
    public Class<SingleTableRuleConfiguration> getRuleConfigurationClass() {
        return SingleTableRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterDefaultSingleTableRuleStatement.class.getName();
    }
}
