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
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.handler.converter.ShadowRuleStatementConverter;
import org.apache.shardingsphere.shadow.distsql.handler.supporter.ShadowRuleStatementSupporter;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowRuleStatement;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Create shadow rule statement updater.
 */
public final class CreateShadowRuleStatementUpdater implements RuleDefinitionCreateUpdater<CreateShadowRuleStatement, ShadowRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        String databaseName = database.getName();
        checkDuplicatedRules(databaseName, sqlStatement, currentRuleConfig);
        checkStorageUnits(database, sqlStatement.getRules());
        checkAlgorithms(databaseName, sqlStatement.getRules());
        checkAlgorithmType(sqlStatement.getRules());
    }
    
    @Override
    public RuleConfiguration buildToBeCreatedRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final CreateShadowRuleStatement sqlStatement) {
        Collection<ShadowRuleSegment> segments = sqlStatement.getRules();
        if (sqlStatement.isIfNotExists()) {
            Collection<String> toBeCreatedRuleNames = ShadowRuleStatementSupporter.getRuleNames(sqlStatement.getRules());
            toBeCreatedRuleNames.retainAll(ShadowRuleStatementSupporter.getRuleNames(currentRuleConfig));
            segments.removeIf(each -> toBeCreatedRuleNames.contains(each.getRuleName()));
        }
        return ShadowRuleStatementConverter.convert(segments);
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final ShadowRuleConfiguration toBeCreatedRuleConfig) {
        currentRuleConfig.getDataSources().addAll(toBeCreatedRuleConfig.getDataSources());
        currentRuleConfig.getShadowAlgorithms().putAll(toBeCreatedRuleConfig.getShadowAlgorithms());
        updateTables(currentRuleConfig.getTables(), toBeCreatedRuleConfig.getTables());
    }
    
    private void updateTables(final Map<String, ShadowTableConfiguration> currentTables, final Map<String, ShadowTableConfiguration> toBeCreateTables) {
        toBeCreateTables.forEach((key, value) -> currentTables.merge(key, value, ShadowRuleStatementSupporter::mergeConfiguration));
    }
    
    private void checkDuplicatedRules(final String databaseName, final CreateShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        Collection<String> toBeCreatedRuleNames = ShadowRuleStatementSupporter.getRuleNames(sqlStatement.getRules());
        ShadowRuleStatementChecker.checkDuplicated(toBeCreatedRuleNames, duplicated -> new DuplicateRuleException("shadow", databaseName, duplicated));
        if (!sqlStatement.isIfNotExists()) {
            toBeCreatedRuleNames.retainAll(ShadowRuleStatementSupporter.getRuleNames(currentRuleConfig));
            ShardingSpherePreconditions.checkState(toBeCreatedRuleNames.isEmpty(), () -> new DuplicateRuleException("shadow", databaseName, toBeCreatedRuleNames));
        }
    }
    
    private void checkStorageUnits(final ShardingSphereDatabase database, final Collection<ShadowRuleSegment> segments) {
        ShadowRuleStatementChecker.checkStorageUnitsExist(ShadowRuleStatementSupporter.getStorageUnitNames(segments), database);
    }
    
    private void checkAlgorithms(final String databaseName, final Collection<ShadowRuleSegment> segments) {
        ShadowRuleStatementChecker.checkAlgorithmCompleteness(ShadowRuleStatementSupporter.getShadowAlgorithmSegment(segments));
        ShadowRuleStatementChecker.checkDuplicated(ShadowRuleStatementSupporter.getAlgorithmNames(segments), duplicated -> new DuplicateRuleException("shadow", databaseName, duplicated));
    }
    
    private void checkAlgorithmType(final Collection<ShadowRuleSegment> segments) {
        Collection<String> invalidAlgorithmTypes = segments.stream().flatMap(each -> each.getShadowTableRules().values().stream()).flatMap(Collection::stream)
                .map(each -> each.getAlgorithmSegment().getName()).collect(Collectors.toSet())
                .stream().filter(each -> !TypedSPILoader.contains(ShadowAlgorithm.class, each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(invalidAlgorithmTypes.isEmpty(), () -> new InvalidAlgorithmConfigurationException("shadow", invalidAlgorithmTypes));
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
