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

import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropDefaultSingleTableRuleStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;

/**
 * Drop default single table rule statement updater.
 */
public final class DropDefaultSingleTableRuleStatementUpdater implements RuleDefinitionDropUpdater<DropDefaultSingleTableRuleStatement, SingleTableRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database,
                                  final DropDefaultSingleTableRuleStatement sqlStatement, final SingleTableRuleConfiguration currentRuleConfig) throws DistSQLException {
        checkCurrentRuleConfiguration(database.getName(), sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName,
                                               final DropDefaultSingleTableRuleStatement sqlStatement, final SingleTableRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (!sqlStatement.isIfExists()) {
            DistSQLException.predictionThrow(null != currentRuleConfig && currentRuleConfig.getDefaultDataSource().isPresent(), () -> new RequiredRuleMissedException("single table", databaseName));
        }
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropDefaultSingleTableRuleStatement sqlStatement, final SingleTableRuleConfiguration currentRuleConfig) {
        currentRuleConfig.setDefaultDataSource(null);
        return false;
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropDefaultSingleTableRuleStatement sqlStatement, final SingleTableRuleConfiguration currentRuleConfig) {
        return null != currentRuleConfig && currentRuleConfig.getDefaultDataSource().isPresent();
    }
    
    @Override
    public Class<SingleTableRuleConfiguration> getRuleConfigurationClass() {
        return SingleTableRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropDefaultSingleTableRuleStatement.class.getName();
    }
}
