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

package org.apache.shardingsphere.shadow.distsql.handler.update;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateDefaultShadowAlgorithmStatement;

import java.util.Collections;

/**
 * Create default shadow algorithm statement updater.
 */
public final class CreateDefaultShadowAlgorithmStatementUpdater implements RuleDefinitionCreateUpdater<CreateDefaultShadowAlgorithmStatement, ShadowRuleConfiguration> {
    
    @Override
    public RuleConfiguration buildToBeCreatedRuleConfiguration(final CreateDefaultShadowAlgorithmStatement sqlStatement) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setDefaultShadowAlgorithmName(sqlStatement.getAlgorithmName());
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final ShadowRuleConfiguration toBeCreatedRuleConfig) {
        currentRuleConfig.setDefaultShadowAlgorithmName(toBeCreatedRuleConfig.getDefaultShadowAlgorithmName());
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database,
                                  final CreateDefaultShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        checkAlgorithmExist(database.getName(), sqlStatement, currentRuleConfig);
    }
    
    private void checkAlgorithmExist(final String databaseName, final CreateDefaultShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        ShardingSpherePreconditions.checkState(currentRuleConfig.getShadowAlgorithms().containsKey(sqlStatement.getAlgorithmName()),
                () -> new MissingRequiredAlgorithmException(databaseName, Collections.singleton(sqlStatement.getAlgorithmName())));
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getRuleConfigurationClass() {
        return ShadowRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateDefaultShadowAlgorithmStatement.class.getName();
    }
}
