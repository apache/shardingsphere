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

import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.handler.converter.ShadowRuleStatementConverter;
import org.apache.shardingsphere.shadow.distsql.handler.supporter.ShadowRuleStatementSupporter;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowRuleStatement;
import org.apache.shardingsphere.shadow.factory.ShadowAlgorithmFactory;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Create shadow rule statement updater.
 */
public final class CreateShadowRuleStatementUpdater implements RuleDefinitionCreateUpdater<CreateShadowRuleStatement, ShadowRuleConfiguration> {
    
    private static final String SHADOW = "shadow";
    
    private Collection<String> identicalRuleNames;
    
    @Override
    public RuleConfiguration buildToBeCreatedRuleConfiguration(final CreateShadowRuleStatement sqlStatement) {
        Collection<ShadowRuleSegment> rules = sqlStatement.getRules();
        if (null != identicalRuleNames && !identicalRuleNames.isEmpty()) {
            rules = sqlStatement.getRules().stream().filter(each -> !identicalRuleNames.contains(each.getRuleName())).collect(Collectors.toSet());
        }
        return ShadowRuleStatementConverter.convert(rules);
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final ShadowRuleConfiguration toBeCreatedRuleConfig) {
        if (null != currentRuleConfig) {
            currentRuleConfig.getDataSources().addAll(toBeCreatedRuleConfig.getDataSources());
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
        checkRuleNames(databaseName, sqlStatement, currentRuleConfig);
        checkResources(database, rules);
        checkAlgorithms(databaseName, rules);
        checkAlgorithmType(rules);
    }
    
    private void checkRuleNames(final String databaseName, final CreateShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        Collection<String> toBeCreatedRuleNames = ShadowRuleStatementSupporter.getRuleNames(sqlStatement.getRules());
        ShadowRuleStatementChecker.checkAnyDuplicate(toBeCreatedRuleNames, duplicated -> new DuplicateRuleException(SHADOW, databaseName, duplicated));
        toBeCreatedRuleNames.retainAll(ShadowRuleStatementSupporter.getRuleNames(currentRuleConfig));
        if (sqlStatement.isIfNotExists()) {
            identicalRuleNames = toBeCreatedRuleNames;
            return;
        }
        ShardingSpherePreconditions.checkState(toBeCreatedRuleNames.isEmpty(), () -> new DuplicateRuleException(SHADOW, databaseName, toBeCreatedRuleNames));
    }
    
    private void checkResources(final ShardingSphereDatabase database, final Collection<ShadowRuleSegment> rules) {
        ShadowRuleStatementChecker.checkResourceExist(ShadowRuleStatementSupporter.getResourceNames(rules), database);
    }
    
    private void checkAlgorithms(final String databaseName, final Collection<ShadowRuleSegment> rules) {
        ShadowRuleStatementChecker.checkAlgorithmCompleteness(ShadowRuleStatementSupporter.getShadowAlgorithmSegment(rules));
        ShadowRuleStatementChecker.checkAnyDuplicate(ShadowRuleStatementSupporter.getAlgorithmNames(rules), duplicated -> new DuplicateRuleException(SHADOW, databaseName, duplicated));
    }
    
    private void checkAlgorithmType(final Collection<ShadowRuleSegment> rules) {
        Collection<String> nonexistentAlgorithmTypes = rules.stream().flatMap(each -> each.getShadowTableRules().values().stream()).flatMap(Collection::stream)
                .map(each -> each.getAlgorithmSegment().getName()).collect(Collectors.toSet()).stream().filter(each -> !ShadowAlgorithmFactory.contains(each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(nonexistentAlgorithmTypes.isEmpty(), () -> new InvalidAlgorithmConfigurationException(SHADOW, nonexistentAlgorithmTypes));
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
