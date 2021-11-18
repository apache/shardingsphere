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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.rule;

import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AlterDefaultSingleTableRuleStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;

import java.util.Collections;
import java.util.Set;

/**
 * Alter default single table rule statement updater.
 */
public final class AlterDefaultSingleTableRuleStatementUpdater implements RuleDefinitionAlterUpdater<AlterDefaultSingleTableRuleStatement, SingleTableRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final AlterDefaultSingleTableRuleStatement sqlStatement,
                                  final SingleTableRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkConfigurationExist(schemaName, currentRuleConfig);
        checkResourceExist(schemaName, shardingSphereMetaData, sqlStatement);
        checkDefaultResourceExist(schemaName, currentRuleConfig);
    }
    
    private void checkConfigurationExist(final String schemaName, final SingleTableRuleConfiguration currentRuleConfig) throws DistSQLException {
        DistSQLException.predictionThrow(null != currentRuleConfig, new RequiredRuleMissedException(schemaName, "single table"));
    }
    
    private void checkResourceExist(final String schemaName, final ShardingSphereMetaData metaData, final AlterDefaultSingleTableRuleStatement sqlStatement) throws DistSQLException {
        Set<String> resourceNames = metaData.getResource().getDataSources().keySet();
        DistSQLException.predictionThrow(resourceNames.contains(sqlStatement.getDefaultResource()),
                new RequiredResourceMissedException(schemaName, Collections.singleton(sqlStatement.getDefaultResource())));
    }
    
    private void checkDefaultResourceExist(final String schemaName, final SingleTableRuleConfiguration currentRuleConfig) throws DistSQLException {
        DistSQLException.predictionThrow(currentRuleConfig.getDefaultDataSource().isPresent(), new RequiredRuleMissedException("single table", schemaName));
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
        return AlterDefaultSingleTableRuleStatement.class.getCanonicalName();
    }
}
