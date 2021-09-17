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

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.AlgorithmInUsedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.handler.converter.ShadowRuleStatementConverter;
import org.apache.shardingsphere.shadow.distsql.handler.supporter.ShadowRuleStatementSupporter;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowRuleStatement;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
            currentRuleConfig.getTables().putAll(toBeCreatedRuleConfig.getTables());
        }
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData metaData, final CreateShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = metaData.getName();
        Collection<ShadowRuleSegment> rules = sqlStatement.getRules();
        checkRuleNames(schemaName, rules, currentRuleConfig);
        checkResources(schemaName, rules, currentRuleConfig, metaData);
        checkTables(schemaName, rules, currentRuleConfig);
        checkAlgorithms(schemaName, rules, currentRuleConfig);
    }
    
    private void checkRuleNames(final String schemaName, final Collection<ShadowRuleSegment> rules, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        List<String> requireRuleNames = ShadowRuleStatementSupporter.getRuleName(rules);
        ShadowRuleStatementChecker.checkDuplicate(requireRuleNames, duplicate -> new DuplicateRuleException(SHADOW, schemaName, duplicate));
        List<String> currentRuleName = ShadowRuleStatementSupporter.getRuleName(currentRuleConfig);
        ShadowRuleStatementChecker.checkIdentical(requireRuleNames, currentRuleName, identical -> new DuplicateRuleException(SHADOW, schemaName, identical));
    }
    
    private void checkTables(final String schemaName, final Collection<ShadowRuleSegment> rules, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        List<String> requireTables = ShadowRuleStatementSupporter.getTable(rules);
        ShadowRuleStatementChecker.checkDuplicate(requireTables, duplicate -> new DuplicateRuleException(SHADOW, schemaName, duplicate));
        List<String> currentTables = ShadowRuleStatementSupporter.getTable(currentRuleConfig);
        ShadowRuleStatementChecker.checkIdentical(requireTables, currentTables, identical -> new DuplicateRuleException(SHADOW, schemaName, identical));
    }
    
    private void checkResources(final String schemaName, final Collection<ShadowRuleSegment> rules, final ShadowRuleConfiguration currentRuleConfig,
                                final ShardingSphereMetaData metaData) throws DistSQLException {
        List<String> requireSourceResource = ShadowRuleStatementSupporter.getSourceResource(rules);
        ShadowRuleStatementChecker.checkDuplicate(requireSourceResource, duplicate -> new DuplicateRuleException(SHADOW, schemaName, duplicate));
        List<String> currentSourceResource = ShadowRuleStatementSupporter.getSourceResource(currentRuleConfig);
        ShadowRuleStatementChecker.checkIdentical(requireSourceResource, currentSourceResource, identical -> new DuplicateRuleException(SHADOW, schemaName, identical));
        List<String> requireResource = ShadowRuleStatementSupporter.getResource(rules);
        ShadowRuleStatementChecker.checkResourceExist(requireResource, metaData, schemaName);
    }
    
    private void checkAlgorithms(final String schemaName, final Collection<ShadowRuleSegment> rules, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        List<ShadowAlgorithmSegment> requireAlgorithms = getShadowAlgorithmSegment(rules);
        ShadowRuleStatementChecker.checkAlgorithmCompleteness(requireAlgorithms);
        List<String> requireAlgorithmNames = ShadowRuleStatementSupporter.getAlgorithm(rules);
        ShadowRuleStatementChecker.checkDuplicate(requireAlgorithmNames, duplicate -> new AlgorithmInUsedException(schemaName, duplicate));
        List<String> currentAlgorithmNames = ShadowRuleStatementSupporter.getAlgorithm(currentRuleConfig);
        ShadowRuleStatementChecker.checkIdentical(requireAlgorithmNames, currentAlgorithmNames, identical -> new AlgorithmInUsedException(schemaName, identical));
    }
    
    private List<ShadowAlgorithmSegment> getShadowAlgorithmSegment(final Collection<ShadowRuleSegment> rules) {
        return rules.stream().flatMap(each -> each.getShadowTableRules().values().stream()).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getRuleConfigurationClass() {
        return ShadowRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateShadowRuleStatement.class.getCanonicalName();
    }
}
