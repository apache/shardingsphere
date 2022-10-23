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

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.factory.ShadowAlgorithmFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Create default shadow algorithm statement updater.
 */
public final class CreateDefaultShadowAlgorithmStatementUpdater implements RuleDefinitionCreateUpdater<CreateDefaultShadowAlgorithmStatement, ShadowRuleConfiguration> {
    
    private static final String SHADOW = "shadow";
    
    @Override
    public RuleConfiguration buildToBeCreatedRuleConfiguration(final CreateDefaultShadowAlgorithmStatement sqlStatement) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(buildAlgorithmMap(sqlStatement));
        result.setDefaultShadowAlgorithmName(sqlStatement.getShadowAlgorithmSegment().getAlgorithmName());
        return result;
    }
    
    private Map<String, AlgorithmConfiguration> buildAlgorithmMap(final CreateDefaultShadowAlgorithmStatement sqlStatement) {
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration(sqlStatement.getShadowAlgorithmSegment().getAlgorithmSegment().getName(),
                sqlStatement.getShadowAlgorithmSegment().getAlgorithmSegment().getProps());
        return Collections.singletonMap(sqlStatement.getShadowAlgorithmSegment().getAlgorithmName(), algorithmConfig);
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final ShadowRuleConfiguration toBeCreatedRuleConfig) {
        currentRuleConfig.getShadowAlgorithms().putAll(toBeCreatedRuleConfig.getShadowAlgorithms());
        currentRuleConfig.setDefaultShadowAlgorithmName(toBeCreatedRuleConfig.getDefaultShadowAlgorithmName());
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateDefaultShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        ShadowRuleStatementChecker.checkAlgorithmCompleteness(Collections.singleton(sqlStatement.getShadowAlgorithmSegment()));
        checkExist(database.getName(), sqlStatement, currentRuleConfig);
        checkAlgorithmType(sqlStatement);
    }
    
    private void checkExist(final String databaseName, final CreateDefaultShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        if (null == currentRuleConfig) {
            return;
        }
        Collection<String> requireAlgorithmNames = Collections.singleton(sqlStatement.getShadowAlgorithmSegment().getAlgorithmName());
        ShadowRuleStatementChecker.checkAnyDuplicate(requireAlgorithmNames,
                currentRuleConfig.getShadowAlgorithms().keySet(), different -> new DuplicateRuleException(SHADOW, databaseName, different));
    }
    
    private void checkAlgorithmType(final CreateDefaultShadowAlgorithmStatement sqlStatement) {
        String shadowAlgorithmType = sqlStatement.getShadowAlgorithmSegment().getAlgorithmSegment().getName();
        ShardingSpherePreconditions.checkState(ShadowAlgorithmFactory.contains(shadowAlgorithmType),
                () -> new InvalidAlgorithmConfigurationException(SHADOW, shadowAlgorithmType));
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
