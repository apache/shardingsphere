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
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.AlgorithmInUsedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.converter.ShadowRuleStatementConverter;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.AlterShadowRuleStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Alter shadow rule statement updater.
 */
public final class AlterShadowRuleStatementUpdater implements RuleDefinitionAlterUpdater<AlterShadowRuleStatement, ShadowRuleConfiguration> {
    
    private static final String SHADOW = "shadow";
    
    private static final String RULE_NAME = "ruleName";
    
    private static final String TABLE = "table";
    
    private static final String RESOURCE = "resource";
    
    private static final String ALGORITHM = "algorithm";
    
    @Override
    public RuleConfiguration buildToBeAlteredRuleConfiguration(final AlterShadowRuleStatement sqlStatement) {
        return ShadowRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final ShadowRuleConfiguration toBeAlteredRuleConfig) {
        currentRuleConfig.getDataSources().putAll(toBeAlteredRuleConfig.getDataSources());
        currentRuleConfig.getShadowAlgorithms().putAll(toBeAlteredRuleConfig.getShadowAlgorithms());
        currentRuleConfig.setTables(toBeAlteredRuleConfig.getTables());
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData metaData, final AlterShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = metaData.getName();
        checkConfigurationExist(schemaName, currentRuleConfig);
        checkRuleNames(schemaName, sqlStatement, currentRuleConfig);
        checkResources(schemaName, sqlStatement, metaData);
        checkTables(schemaName, sqlStatement);
        checkAlgorithms(schemaName, sqlStatement);
    }
    
    private void checkConfigurationExist(final String schemaName, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        DistSQLException.predictionThrow(null != currentRuleConfig, new RequiredRuleMissedException(SHADOW, schemaName));
    }
    
    private void checkRuleNames(final String schemaName, final AlterShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        List<String> currentRuleNames = getProperties(currentRuleConfig, RULE_NAME);
        List<String> requireRuleNames = getProperties(sqlStatement, RULE_NAME);
        checkDuplicate(requireRuleNames, duplicate -> new DuplicateRuleException(SHADOW, schemaName, duplicate));
        checkDifferent(requireRuleNames, currentRuleNames, different -> new InvalidAlgorithmConfigurationException("shadow rule name ", different));
    }
    
    private void checkTables(final String schemaName, final AlterShadowRuleStatement sqlStatement) throws DistSQLException {
        List<String> requireTables = getProperties(sqlStatement, TABLE);
        checkDuplicate(requireTables, duplicate -> new DuplicateRuleException(SHADOW, schemaName, duplicate));
    }
    
    private void checkResources(final String schemaName, final AlterShadowRuleStatement sqlStatement, final ShardingSphereMetaData metaData) throws DistSQLException {
        List<String> requireSourceResources = getProperties(sqlStatement, RESOURCE);
        checkDuplicate(requireSourceResources, duplicate -> new DuplicateRuleException(SHADOW, schemaName, duplicate));
        checkResourceExist(sqlStatement.getRules(), metaData, schemaName);
    }
    
    private void checkAlgorithms(final String schemaName, final AlterShadowRuleStatement sqlStatement) throws DistSQLException {
        checkAlgorithmCompleteness(sqlStatement);
        List<String> requireAlgorithmNames = getProperties(sqlStatement, ALGORITHM);
        checkDuplicate(requireAlgorithmNames, duplicate -> new AlgorithmInUsedException(schemaName, duplicate));
    }
    
    private void checkAlgorithmCompleteness(final AlterShadowRuleStatement sqlStatement) throws DistSQLException {
        List<ShadowAlgorithmSegment> incompleteAlgorithms = getShadowAlgorithmSegment(sqlStatement).flatMap(Collection::stream).filter(each -> !each.isComplete()).collect(Collectors.toList());
        DistSQLException.predictionThrow(incompleteAlgorithms.isEmpty(), new InvalidAlgorithmConfigurationException(SHADOW));
    }
    
    private List<String> getProperties(final ShadowRuleConfiguration currentRuleConfig, final String propName) {
        if (RULE_NAME.equals(propName)) {
            return new ArrayList<>(currentRuleConfig.getDataSources().keySet());
        } else if (TABLE.equals(propName)) {
            return new ArrayList<>(currentRuleConfig.getTables().keySet());
        } else if (RESOURCE.equals(propName)) {
            return new ArrayList<>(currentRuleConfig.getSourceDataSourceNames());
        } else if (ALGORITHM.equals(propName)) {
            return new ArrayList<>(currentRuleConfig.getShadowAlgorithms().keySet());
        }
        return Collections.emptyList();
    }
    
    private List<String> getProperties(final AlterShadowRuleStatement sqlStatement, final String propName) {
        if (RULE_NAME.equals(propName)) {
            return sqlStatement.getRules().stream().map(ShadowRuleSegment::getRuleName).collect(Collectors.toList());
        } else if (TABLE.equals(propName)) {
            return sqlStatement.getRules().stream().flatMap(each -> each.getShadowTableRules().keySet().stream()).collect(Collectors.toList());
        } else if (RESOURCE.equals(propName)) {
            return sqlStatement.getRules().stream().map(ShadowRuleSegment::getSource).collect(Collectors.toList());
        } else if (ALGORITHM.equals(propName)) {
            return getShadowAlgorithmSegment(sqlStatement).flatMap(Collection::stream).map(ShadowAlgorithmSegment::getAlgorithmName).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
    
    private Stream<Collection<ShadowAlgorithmSegment>> getShadowAlgorithmSegment(final AlterShadowRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().flatMap(each -> each.getShadowTableRules().values().stream());
    }
    
    private void checkDuplicate(final List<String> require, final Function<Set<String>, DistSQLException> thrower) throws DistSQLException {
        Set<String> duplicateRequire = getDuplicate(require);
        DistSQLException.predictionThrow(duplicateRequire.isEmpty(), thrower.apply(duplicateRequire));
    }
    
    private void checkDifferent(final List<String> require, final List<String> current, final Function<Set<String>, DistSQLException> thrower) throws DistSQLException {
        Set<String> duplicateRequire = getDifferent(require, current);
        DistSQLException.predictionThrow(duplicateRequire.isEmpty(), thrower.apply(duplicateRequire));
    }
    
    private Set<String> getDuplicate(final List<String> requires) {
        return requires.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
    }
    
    private Set<String> getDifferent(final List<String> require, final List<String> current) {
        return require.stream().filter(each -> !current.contains(each)).collect(Collectors.toSet());
    }
    
    private void checkResourceExist(final Collection<ShadowRuleSegment> rules, final ShardingSphereMetaData metaData, final String schemaName) throws DistSQLException {
        List<String> requireResource = rules.stream().map(each -> Arrays.asList(each.getSource(), each.getShadow())).flatMap(Collection::stream).collect(Collectors.toList());
        Collection<String> notExistedResources = metaData.getResource().getNotExistedResources(requireResource);
        DistSQLException.predictionThrow(notExistedResources.isEmpty(), new RequiredResourceMissedException(schemaName, notExistedResources));
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
