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

import org.apache.shardingsphere.distsql.handler.exception.algorithm.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropDefaultShadowAlgorithmStatement;

import java.util.Collections;

/**
 * Drop default shadow algorithm statement updater.
 */
public final class DropDefaultShadowAlgorithmStatementUpdater implements RuleDefinitionDropUpdater<DropDefaultShadowAlgorithmStatement, ShadowRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final DropDefaultShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        if (sqlStatement.isIfExists() && !isExistRuleConfig(currentRuleConfig)) {
            return;
        }
        ShadowRuleStatementChecker.checkRuleConfigurationExists(database.getName(), currentRuleConfig);
        checkAlgorithm(database.getName(), sqlStatement, currentRuleConfig);
    }
    
    private void checkAlgorithm(final String databaseName, final DropDefaultShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        if (!sqlStatement.isIfExists()) {
            ShardingSpherePreconditions.checkNotNull(currentRuleConfig.getDefaultShadowAlgorithmName(),
                    () -> new MissingRequiredAlgorithmException("shadow", databaseName, Collections.singleton("default")));
        }
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropDefaultShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        return null != currentRuleConfig && null != currentRuleConfig.getDefaultShadowAlgorithmName();
    }
    
    @Override
    public ShadowRuleConfiguration buildToBeDroppedRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final DropDefaultShadowAlgorithmStatement sqlStatement) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(Collections.singletonMap(currentRuleConfig.getDefaultShadowAlgorithmName(),
                currentRuleConfig.getShadowAlgorithms().get(currentRuleConfig.getDefaultShadowAlgorithmName())));
        result.setDefaultShadowAlgorithmName(currentRuleConfig.getDefaultShadowAlgorithmName());
        return result;
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropDefaultShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getShadowAlgorithms().remove(currentRuleConfig.getDefaultShadowAlgorithmName());
        currentRuleConfig.setDefaultShadowAlgorithmName(null);
        return currentRuleConfig.isEmpty();
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getRuleConfigurationClass() {
        return ShadowRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropDefaultShadowAlgorithmStatement.class.getName();
    }
}
