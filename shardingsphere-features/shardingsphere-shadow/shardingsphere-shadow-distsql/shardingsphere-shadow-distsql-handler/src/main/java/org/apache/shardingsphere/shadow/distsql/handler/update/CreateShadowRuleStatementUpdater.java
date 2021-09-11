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
import org.apache.shardingsphere.infra.distsql.exception.resource.ResourceInUsedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.AlgorithmInUsedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.converter.ShadowRuleStatementConverter;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowRuleStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        checkRuleNames(schemaName, sqlStatement, currentRuleConfig);
        checkResources(schemaName, sqlStatement, currentRuleConfig, metaData);
        checkTables(schemaName, sqlStatement, currentRuleConfig);
        checkAlgorithms(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkRuleNames(final String schemaName, final CreateShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        List<String> requireRuleNames = sqlStatement.getRules().stream().map(ShadowRuleSegment::getRuleName).collect(Collectors.toList());
        Set<String> duplicatedNames = getDuplicateInRequirement(requireRuleNames);
        DistSQLException.predictionThrow(duplicatedNames.isEmpty(), new DuplicateRuleException(SHADOW, schemaName, duplicatedNames));
        if (null != currentRuleConfig) {
            duplicatedNames.addAll(currentRuleConfig.getDataSources().keySet().stream().filter(requireRuleNames::contains).collect(Collectors.toList()));
            DistSQLException.predictionThrow(duplicatedNames.isEmpty(), new DuplicateRuleException(SHADOW, schemaName, duplicatedNames));
        }
    }
    
    private void checkTables(final String schemaName, final CreateShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        List<String> requireTables = sqlStatement.getRules().stream().flatMap(each -> each.getShadowTableRules().keySet().stream()).collect(Collectors.toList());
        Set<String> duplicatedTables = getDuplicateInRequirement(requireTables);
        DistSQLException.predictionThrow(duplicatedTables.isEmpty(), new DuplicateRuleException(SHADOW, schemaName, duplicatedTables));
        if (null != currentRuleConfig) {
            duplicatedTables.addAll(currentRuleConfig.getTables().keySet().stream().filter(requireTables::contains).collect(Collectors.toList()));
            DistSQLException.predictionThrow(duplicatedTables.isEmpty(), new DuplicateRuleException(SHADOW, schemaName, duplicatedTables));
        }
    }
    
    private void checkResources(final String schemaName, final CreateShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig,
                                final ShardingSphereMetaData metaData) throws DistSQLException {
        List<String> requireSourceResource = sqlStatement.getRules().stream().map(ShadowRuleSegment::getSource).collect(Collectors.toList());
        Set<String> duplicatedSource = getDuplicateInRequirement(requireSourceResource);
        if (null != currentRuleConfig) {
            duplicatedSource.addAll(currentRuleConfig.getSourceDataSourceNames().stream().filter(requireSourceResource::contains).collect(Collectors.toList()));
        }
        DistSQLException.predictionThrow(duplicatedSource.isEmpty(), new ResourceInUsedException(duplicatedSource));
        List<String> requireResource = sqlStatement.getRules().stream().map(each -> Arrays.asList(each.getSource(), each.getShadow())).flatMap(Collection::stream).collect(Collectors.toList());
        Collection<String> notExistedResources = metaData.getResource().getNotExistedResources(requireResource);
        DistSQLException.predictionThrow(notExistedResources.isEmpty(), new RequiredResourceMissedException(schemaName, notExistedResources));
    }
    
    private void checkAlgorithms(final String schemaName, final CreateShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        List<ShadowAlgorithmSegment> incompleteAlgorithms = getShadowAlgorithmSegment(sqlStatement).flatMap(Collection::stream).filter(each -> !each.isComplete()).collect(Collectors.toList());
        DistSQLException.predictionThrow(incompleteAlgorithms.isEmpty(), new InvalidAlgorithmConfigurationException(SHADOW));
        List<String> requireAlgorithmNames = getShadowAlgorithmSegment(sqlStatement).flatMap(Collection::stream).map(ShadowAlgorithmSegment::getAlgorithmName).collect(Collectors.toList());
        Set<String> duplicatedAlgorithmName = getDuplicateInRequirement(requireAlgorithmNames);
        DistSQLException.predictionThrow(duplicatedAlgorithmName.isEmpty(), new AlgorithmInUsedException(schemaName, duplicatedAlgorithmName));
        if (null != currentRuleConfig) {
            duplicatedAlgorithmName.addAll(currentRuleConfig.getShadowAlgorithms().keySet().stream().filter(requireAlgorithmNames::contains).collect(Collectors.toList()));
        }
        DistSQLException.predictionThrow(duplicatedAlgorithmName.isEmpty(), new AlgorithmInUsedException(schemaName, duplicatedAlgorithmName));
    }
    
    private Stream<Collection<ShadowAlgorithmSegment>> getShadowAlgorithmSegment(final CreateShadowRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().flatMap(each -> each.getShadowTableRules().values().stream());
    }
    
    private Set<String> getDuplicateInRequirement(final List<String> requires) {
        return requires.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
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
