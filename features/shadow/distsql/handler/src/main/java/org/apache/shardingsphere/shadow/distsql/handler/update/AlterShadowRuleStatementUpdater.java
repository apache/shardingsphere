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
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.rule.AlgorithmInUsedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.handler.converter.ShadowRuleStatementConverter;
import org.apache.shardingsphere.shadow.distsql.handler.supporter.ShadowRuleStatementSupporter;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.AlterShadowRuleStatement;

import java.util.Collection;
import java.util.Map;

/**
 * Alter shadow rule statement updater.
 */
public final class AlterShadowRuleStatementUpdater implements RuleDefinitionAlterUpdater<AlterShadowRuleStatement, ShadowRuleConfiguration> {
    
    private static final String SHADOW = "shadow";
    
    @Override
    public RuleConfiguration buildToBeAlteredRuleConfiguration(final AlterShadowRuleStatement sqlStatement) {
        return ShadowRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final ShadowRuleConfiguration toBeAlteredRuleConfig) {
        updateDataSources(currentRuleConfig, toBeAlteredRuleConfig.getDataSources());
        updateTables(currentRuleConfig.getTables(), toBeAlteredRuleConfig.getTables());
        currentRuleConfig.getShadowAlgorithms().putAll(toBeAlteredRuleConfig.getShadowAlgorithms());
    }
    
    private void updateDataSources(final ShadowRuleConfiguration currentRuleConfig, final Map<String, ShadowDataSourceConfiguration> toBeAlteredDataSources) {
        currentRuleConfig.getTables().values().forEach(each -> each.getDataSourceNames().removeIf(toBeAlteredDataSources::containsKey));
        currentRuleConfig.getDataSources().putAll(toBeAlteredDataSources);
    }
    
    private void updateTables(final Map<String, ShadowTableConfiguration> currentTables, final Map<String, ShadowTableConfiguration> toBeAlteredTables) {
        toBeAlteredTables.forEach(currentTables::replace);
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final AlterShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        String databaseName = database.getName();
        Collection<ShadowRuleSegment> rules = sqlStatement.getRules();
        checkConfigurationExist(databaseName, currentRuleConfig);
        checkRuleNames(databaseName, rules, currentRuleConfig);
        checkResources(database, rules);
        checkAlgorithms(databaseName, rules);
    }
    
    private void checkConfigurationExist(final String databaseName, final DatabaseRuleConfiguration currentRuleConfig) {
        ShadowRuleStatementChecker.checkConfigurationExist(databaseName, currentRuleConfig);
    }
    
    private void checkRuleNames(final String databaseName, final Collection<ShadowRuleSegment> rules, final ShadowRuleConfiguration currentRuleConfig) {
        Collection<String> currentRuleNames = ShadowRuleStatementSupporter.getRuleNames(currentRuleConfig);
        Collection<String> requireRuleNames = ShadowRuleStatementSupporter.getRuleNames(rules);
        ShadowRuleStatementChecker.checkAnyDuplicate(requireRuleNames, duplicated -> new DuplicateRuleException(SHADOW, databaseName, duplicated));
        ShadowRuleStatementChecker.checkRulesExist(requireRuleNames, currentRuleNames, different -> new InvalidAlgorithmConfigurationException("shadow rule name ", different));
    }
    
    private void checkResources(final ShardingSphereDatabase database, final Collection<ShadowRuleSegment> rules) {
        Collection<String> requireResource = ShadowRuleStatementSupporter.getResourceNames(rules);
        ShadowRuleStatementChecker.checkResourceExist(requireResource, database);
    }
    
    private void checkAlgorithms(final String databaseName, final Collection<ShadowRuleSegment> rules) {
        Collection<ShadowAlgorithmSegment> shadowAlgorithmSegment = ShadowRuleStatementSupporter.getShadowAlgorithmSegment(rules);
        ShadowRuleStatementChecker.checkAlgorithmCompleteness(shadowAlgorithmSegment);
        Collection<String> requireAlgorithms = ShadowRuleStatementSupporter.getAlgorithmNames(rules);
        ShadowRuleStatementChecker.checkAnyDuplicate(requireAlgorithms, duplicated -> new AlgorithmInUsedException("Shadow", databaseName, duplicated));
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getRuleConfigurationClass() {
        return ShadowRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterShadowRuleStatement.class.getName();
    }
}
