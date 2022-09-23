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

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.factory.ShadowAlgorithmFactory;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Create shadow algorithm statement updater.
 */
public final class CreateShadowAlgorithmStatementUpdater implements RuleDefinitionCreateUpdater<CreateShadowAlgorithmStatement, ShadowRuleConfiguration> {
    
    private static final String SHADOW = "shadow";
    
    @Override
    public RuleConfiguration buildToBeCreatedRuleConfiguration(final CreateShadowAlgorithmStatement sqlStatement) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(buildAlgorithmMap(sqlStatement));
        return result;
    }
    
    private Map<String, AlgorithmConfiguration> buildAlgorithmMap(final CreateShadowAlgorithmStatement sqlStatement) {
        return sqlStatement.getAlgorithms().stream().collect(Collectors.toMap(ShadowAlgorithmSegment::getAlgorithmName, each -> new AlgorithmConfiguration(
                each.getAlgorithmSegment().getName(), each.getAlgorithmSegment().getProps())));
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final ShadowRuleConfiguration toBeAlteredRuleConfig) {
        currentRuleConfig.getShadowAlgorithms().putAll(toBeAlteredRuleConfig.getShadowAlgorithms());
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        ShadowRuleStatementChecker.checkAlgorithmCompleteness(sqlStatement.getAlgorithms());
        checkDuplicatedInput(database.getName(), sqlStatement);
        checkExist(database.getName(), sqlStatement, currentRuleConfig);
        checkAlgorithmType(sqlStatement);
    }
    
    private void checkAlgorithmType(final CreateShadowAlgorithmStatement sqlStatement) throws DistSQLException {
        Collection<String> notExistedShadowAlgorithms = sqlStatement.getAlgorithms().stream().map(ShadowAlgorithmSegment::getAlgorithmSegment).map(AlgorithmSegment::getName)
                .filter(each -> !ShadowAlgorithmFactory.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedShadowAlgorithms.isEmpty(), () -> new InvalidAlgorithmConfigurationException(SHADOW, notExistedShadowAlgorithms));
    }
    
    private void checkDuplicatedInput(final String databaseName, final CreateShadowAlgorithmStatement sqlStatement) throws DistSQLException {
        Collection<String> requireAlgorithmNames = sqlStatement.getAlgorithms().stream().map(ShadowAlgorithmSegment::getAlgorithmName).collect(Collectors.toList());
        ShadowRuleStatementChecker.checkAnyDuplicate(requireAlgorithmNames, duplicated -> new DuplicateRuleException(SHADOW, databaseName, duplicated));
    }
    
    private void checkExist(final String databaseName, final CreateShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (null == currentRuleConfig) {
            return;
        }
        Collection<String> requireAlgorithmNames = sqlStatement.getAlgorithms().stream().map(ShadowAlgorithmSegment::getAlgorithmName).collect(Collectors.toList());
        ShadowRuleStatementChecker.checkAnyDuplicate(requireAlgorithmNames,
                currentRuleConfig.getShadowAlgorithms().keySet(), different -> new DuplicateRuleException(SHADOW, databaseName, different));
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getRuleConfigurationClass() {
        return ShadowRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateShadowAlgorithmStatement.class.getName();
    }
}
