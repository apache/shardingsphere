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
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.handler.converter.ShadowRuleStatementConverter;
import org.apache.shardingsphere.shadow.distsql.handler.supporter.ShadowRuleStatementSupporter;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowRuleStatement;

import java.util.Collection;
import java.util.Map;

/**
 * Create shadow rule statement updater.
 */
public final class CreateShadowRuleStatementUpdater implements RuleDefinitionCreateUpdater<CreateShadowRuleStatement, ShadowRuleConfiguration> {
    
    private static final String SHADOW = "shadow";
    
    @Override
    public RuleConfiguration buildToBeCreatedRuleConfiguration(final CreateShadowRuleStatement sqlStatement) {
        return ShadowRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final ShadowRuleConfiguration toBeCreatedRuleConfig) {
        if (null != currentRuleConfig) {
            currentRuleConfig.getDataSources().putAll(toBeCreatedRuleConfig.getDataSources());
            currentRuleConfig.getShadowAlgorithms().putAll(toBeCreatedRuleConfig.getShadowAlgorithms());
            updateTables(currentRuleConfig.getTables(), toBeCreatedRuleConfig.getTables());
        }
    }
    
    private void updateTables(final Map<String, ShadowTableConfiguration> currentTables, final Map<String, ShadowTableConfiguration> toBeCreateTables) {
        toBeCreateTables.forEach((key, value) -> currentTables.merge(key, value, ShadowRuleStatementSupporter::mergeConfiguration));
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        String databaseName = database.getName();
        Collection<ShadowRuleSegment> rules = sqlStatement.getRules();
        checkRuleNames(databaseName, rules, currentRuleConfig);
        checkResources(database, rules);
        checkAlgorithms(databaseName, rules, currentRuleConfig);
    }
    
    private void checkRuleNames(final String databaseName, final Collection<ShadowRuleSegment> rules, final ShadowRuleConfiguration currentRuleConfig) {
        Collection<String> requireRuleNames = ShadowRuleStatementSupporter.getRuleNames(rules);
        ShadowRuleStatementChecker.checkAnyDuplicate(requireRuleNames, duplicated -> new DuplicateRuleException(SHADOW, databaseName, duplicated));
        Collection<String> currentRuleName = ShadowRuleStatementSupporter.getRuleNames(currentRuleConfig);
        ShadowRuleStatementChecker.checkAnyDuplicate(requireRuleNames, currentRuleName, identical -> new DuplicateRuleException(SHADOW, databaseName, identical));
    }
    
    private void checkResources(final ShardingSphereDatabase database, final Collection<ShadowRuleSegment> rules) {
        Collection<String> requireResource = ShadowRuleStatementSupporter.getResourceNames(rules);
        ShadowRuleStatementChecker.checkResourceExist(requireResource, database);
    }
    
    private void checkAlgorithms(final String databaseName, final Collection<ShadowRuleSegment> rules, final ShadowRuleConfiguration currentRuleConfig) {
        Collection<ShadowAlgorithmSegment> requireAlgorithms = ShadowRuleStatementSupporter.getShadowAlgorithmSegment(rules);
        ShadowRuleStatementChecker.checkAlgorithmCompleteness(requireAlgorithms);
        Collection<String> requireAlgorithmNames = ShadowRuleStatementSupporter.getAlgorithmNames(rules);
        ShadowRuleStatementChecker.checkAnyDuplicate(requireAlgorithmNames, duplicated -> new DuplicateRuleException(SHADOW, databaseName, duplicated));
        Collection<String> currentAlgorithmNames = ShadowRuleStatementSupporter.getAlgorithmNames(currentRuleConfig);
        ShadowRuleStatementChecker.checkAnyDuplicate(requireAlgorithmNames, currentAlgorithmNames, duplicated -> new DuplicateRuleException(SHADOW, databaseName, duplicated));
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getRuleConfigurationClass() {
        return ShadowRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateShadowRuleStatement.class.getName();
    }
}
