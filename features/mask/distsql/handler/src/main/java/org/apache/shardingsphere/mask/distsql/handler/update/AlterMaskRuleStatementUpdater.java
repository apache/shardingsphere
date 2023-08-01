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

package org.apache.shardingsphere.mask.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.handler.converter.MaskRuleStatementConverter;
import org.apache.shardingsphere.mask.distsql.parser.segment.MaskRuleSegment;
import org.apache.shardingsphere.mask.distsql.parser.statement.AlterMaskRuleStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Alter mask rule statement updater.
 */
public final class AlterMaskRuleStatementUpdater implements RuleDefinitionAlterUpdater<AlterMaskRuleStatement, MaskRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final AlterMaskRuleStatement sqlStatement, final MaskRuleConfiguration currentRuleConfig) {
        String databaseName = database.getName();
        checkCurrentRuleConfiguration(databaseName, currentRuleConfig);
        checkToBeAlteredRules(databaseName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final MaskRuleConfiguration currentRuleConfig) {
        ShardingSpherePreconditions.checkNotNull(currentRuleConfig, () -> new MissingRequiredRuleException("Mask", databaseName));
    }
    
    private void checkToBeAlteredRules(final String databaseName, final AlterMaskRuleStatement sqlStatement, final MaskRuleConfiguration currentRuleConfig) {
        Collection<String> currentMaskTableNames = currentRuleConfig.getTables().stream().map(MaskTableRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedMaskTableNames = getToBeAlteredMaskTableNames(sqlStatement).stream().filter(each -> !currentMaskTableNames.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedMaskTableNames.isEmpty(), () -> new MissingRequiredRuleException("Mask", databaseName, notExistedMaskTableNames));
    }
    
    private Collection<String> getToBeAlteredMaskTableNames(final AlterMaskRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().map(MaskRuleSegment::getTableName).collect(Collectors.toList());
    }
    
    @Override
    public MaskRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterMaskRuleStatement sqlStatement) {
        return MaskRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public MaskRuleConfiguration buildToBeDroppedRuleConfiguration(final MaskRuleConfiguration currentRuleConfig, final MaskRuleConfiguration toBeAlteredRuleConfig) {
        Collection<String> toBeAlteredTableNames = toBeAlteredRuleConfig.getTables().stream().map(MaskTableRuleConfiguration::getName).collect(Collectors.toList());
        Collection<MaskColumnRuleConfiguration> columns = currentRuleConfig.getTables().stream().filter(each -> !toBeAlteredTableNames.contains(each.getName()))
                .flatMap(each -> each.getColumns().stream()).collect(Collectors.toList());
        columns.addAll(toBeAlteredRuleConfig.getTables().stream().flatMap(each -> each.getColumns().stream()).collect(Collectors.toList()));
        Collection<String> inUsedAlgorithmNames = columns.stream().map(MaskColumnRuleConfiguration::getMaskAlgorithm).collect(Collectors.toSet());
        Map<String, AlgorithmConfiguration> toBeDroppedAlgorithms = new HashMap<>();
        for (String each : currentRuleConfig.getMaskAlgorithms().keySet()) {
            if (!inUsedAlgorithmNames.contains(each)) {
                toBeDroppedAlgorithms.put(each, currentRuleConfig.getMaskAlgorithms().get(each));
            }
        }
        return new MaskRuleConfiguration(Collections.emptyList(), toBeDroppedAlgorithms);
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final MaskRuleConfiguration currentRuleConfig, final MaskRuleConfiguration toBeAlteredRuleConfig) {
        dropRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        addRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        dropUnusedAlgorithms(currentRuleConfig);
    }
    
    private void dropRuleConfiguration(final MaskRuleConfiguration currentRuleConfig, final MaskRuleConfiguration toBeAlteredRuleConfig) {
        Collection<String> toBeAlteredRuleName = toBeAlteredRuleConfig.getTables().stream().map(MaskTableRuleConfiguration::getName).collect(Collectors.toList());
        currentRuleConfig.getTables().removeIf(each -> toBeAlteredRuleName.contains(each.getName()));
    }
    
    private void addRuleConfiguration(final MaskRuleConfiguration currentRuleConfig, final MaskRuleConfiguration toBeAlteredRuleConfig) {
        currentRuleConfig.getTables().addAll(toBeAlteredRuleConfig.getTables());
        currentRuleConfig.getMaskAlgorithms().putAll(toBeAlteredRuleConfig.getMaskAlgorithms());
    }
    
    private void dropUnusedAlgorithms(final MaskRuleConfiguration currentRuleConfig) {
        Collection<String> inUsedAlgorithms = currentRuleConfig.getTables().stream().flatMap(each -> each.getColumns().stream()).map(MaskColumnRuleConfiguration::getMaskAlgorithm)
                .collect(Collectors.toSet());
        Collection<String> unusedAlgorithms = currentRuleConfig.getMaskAlgorithms().keySet().stream().filter(each -> !inUsedAlgorithms.contains(each)).collect(Collectors.toSet());
        unusedAlgorithms.forEach(each -> currentRuleConfig.getMaskAlgorithms().remove(each));
    }
    
    @Override
    public Class<MaskRuleConfiguration> getRuleConfigurationClass() {
        return MaskRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterMaskRuleStatement.class.getName();
    }
}
