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
import org.apache.shardingsphere.infra.config.scope.SchemaRuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.AlgorithmInUsedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
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
import java.util.List;
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
        currentRuleConfig.getTables().values().forEach(tableConfig -> {
            tableConfig.getDataSourceNames().removeIf(toBeAlteredDataSources::containsKey);
        });
        currentRuleConfig.getDataSources().putAll(toBeAlteredDataSources);
    }
    
    private void updateTables(final Map<String, ShadowTableConfiguration> currentTables, final Map<String, ShadowTableConfiguration> toBeAlteredTables) {
        toBeAlteredTables.forEach((key, value) -> currentTables.merge(key, value, ShadowRuleStatementSupporter::mergeConfiguration));
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData metaData, final AlterShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = metaData.getName();
        Collection<ShadowRuleSegment> rules = sqlStatement.getRules();
        checkConfigurationExist(schemaName, currentRuleConfig);
        checkRuleNames(schemaName, rules, currentRuleConfig);
        checkResources(schemaName, rules, metaData);
        checkAlgorithms(schemaName, rules);
    }
    
    private void checkConfigurationExist(final String schemaName, final SchemaRuleConfiguration currentRuleConfig) throws DistSQLException {
        ShadowRuleStatementChecker.checkConfigurationExist(schemaName, currentRuleConfig);
    }
    
    private void checkRuleNames(final String schemaName, final Collection<ShadowRuleSegment> rules, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        List<String> currentRuleNames = ShadowRuleStatementSupporter.getRuleNames(currentRuleConfig);
        List<String> requireRuleNames = ShadowRuleStatementSupporter.getRuleNames(rules);
        ShadowRuleStatementChecker.checkAnyDuplicate(requireRuleNames, duplicate -> new DuplicateRuleException(SHADOW, schemaName, duplicate));
        ShadowRuleStatementChecker.checkRulesExist(requireRuleNames, currentRuleNames, different -> new InvalidAlgorithmConfigurationException("shadow rule name ", different));
    }
    
    private void checkResources(final String schemaName, final Collection<ShadowRuleSegment> rules, final ShardingSphereMetaData metaData) throws DistSQLException {
        List<String> requireResource = ShadowRuleStatementSupporter.getResourceNames(rules);
        ShadowRuleStatementChecker.checkResourceExist(requireResource, metaData, schemaName);
    }
    
    private void checkAlgorithms(final String schemaName, final Collection<ShadowRuleSegment> rules) throws DistSQLException {
        List<ShadowAlgorithmSegment> shadowAlgorithmSegment = ShadowRuleStatementSupporter.getShadowAlgorithmSegment(rules);
        ShadowRuleStatementChecker.checkAlgorithmCompleteness(shadowAlgorithmSegment);
        List<String> requireAlgorithms = ShadowRuleStatementSupporter.getAlgorithmNames(rules);
        ShadowRuleStatementChecker.checkAnyDuplicate(requireAlgorithms, duplicate -> new AlgorithmInUsedException(schemaName, duplicate));
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getRuleConfigurationClass() {
        return ShadowRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterShadowRuleStatement.class.getCanonicalName();
    }
}
