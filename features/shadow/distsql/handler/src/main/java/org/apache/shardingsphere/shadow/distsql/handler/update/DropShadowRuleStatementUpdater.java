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

import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowRuleStatement;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop shadow rule statement updater.
 */
public final class DropShadowRuleStatementUpdater implements RuleDefinitionDropUpdater<DropShadowRuleStatement, ShadowRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final DropShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        if (sqlStatement.isIfExists() && !isExistRuleConfig(currentRuleConfig)) {
            return;
        }
        checkConfigurationExisted(database.getName(), currentRuleConfig);
        checkRuleExisted(database.getName(), sqlStatement, currentRuleConfig);
    }
    
    private void checkConfigurationExisted(final String databaseName, final ShadowRuleConfiguration currentRuleConfig) {
        ShardingSpherePreconditions.checkNotNull(currentRuleConfig, () -> new MissingRequiredRuleException("Shadow", databaseName));
    }
    
    private void checkRuleExisted(final String databaseName, final DropShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        if (!sqlStatement.isIfExists()) {
            ShadowRuleStatementChecker.checkExisted(sqlStatement.getNames(), getDataSourceNames(currentRuleConfig),
                    notExistedRuleNames -> new MissingRequiredRuleException("Shadow", databaseName, notExistedRuleNames));
        }
    }
    
    private Collection<String> getDataSourceNames(final ShadowRuleConfiguration currentRuleConfig) {
        return currentRuleConfig.getDataSources().stream().map(ShadowDataSourceConfiguration::getName).collect(Collectors.toList());
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        return isExistRuleConfig(currentRuleConfig) && !getIdenticalData(sqlStatement.getNames(), getDataSourceNames(currentRuleConfig)).isEmpty();
    }
    
    @Override
    public ShadowRuleConfiguration buildToBeDroppedRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final DropShadowRuleStatement sqlStatement) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        for (String each : sqlStatement.getNames()) {
            result.getDataSources().add(new ShadowDataSourceConfiguration(each, null, null));
            dropRule(currentRuleConfig, each);
        }
        currentRuleConfig.getTables().forEach((key, value) -> value.getDataSourceNames().removeIf(sqlStatement.getNames()::contains));
        for (Entry<String, ShadowTableConfiguration> each : currentRuleConfig.getTables().entrySet()) {
            if (each.getValue().getDataSourceNames().isEmpty()) {
                result.getTables().put(each.getKey(), each.getValue());
            }
        }
        currentRuleConfig.getTables().entrySet().removeIf(entry -> entry.getValue().getDataSourceNames().isEmpty());
        findUnusedAlgorithms(currentRuleConfig).forEach(each -> result.getShadowAlgorithms().put(each, currentRuleConfig.getShadowAlgorithms().get(each)));
        return result;
    }
    
    @Override
    public ShadowRuleConfiguration buildToBeAlteredRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final DropShadowRuleStatement sqlStatement) {
        Map<String, ShadowTableConfiguration> tables = new LinkedHashMap<>();
        Collection<String> toBeDroppedDataSourceNames = sqlStatement.getNames();
        for (Entry<String, ShadowTableConfiguration> each : currentRuleConfig.getTables().entrySet()) {
            if (!toBeDroppedDataSourceNames.containsAll(each.getValue().getDataSourceNames())) {
                List<String> currentDataSources = new LinkedList<>(each.getValue().getDataSourceNames());
                currentDataSources.removeAll(toBeDroppedDataSourceNames);
                tables.put(each.getKey(), new ShadowTableConfiguration(currentDataSources, each.getValue().getShadowAlgorithmNames()));
            }
        }
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setTables(tables);
        return result;
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        for (String each : sqlStatement.getNames()) {
            dropRule(currentRuleConfig, each);
        }
        currentRuleConfig.getTables().forEach((key, value) -> value.getDataSourceNames().removeIf(sqlStatement.getNames()::contains));
        currentRuleConfig.getTables().entrySet().removeIf(entry -> entry.getValue().getDataSourceNames().isEmpty());
        dropUnusedAlgorithm(currentRuleConfig);
        return currentRuleConfig.getDataSources().isEmpty() || currentRuleConfig.getTables().isEmpty();
    }
    
    private void dropRule(final ShadowRuleConfiguration currentRuleConfig, final String ruleName) {
        Optional<ShadowDataSourceConfiguration> dataSourceRuleConfig = currentRuleConfig.getDataSources().stream().filter(each -> ruleName.equals(each.getName())).findAny();
        dataSourceRuleConfig.ifPresent(optional -> currentRuleConfig.getDataSources().remove(optional));
    }
    
    private void dropUnusedAlgorithm(final ShadowRuleConfiguration currentRuleConfig) {
        findUnusedAlgorithms(currentRuleConfig).forEach(each -> currentRuleConfig.getShadowAlgorithms().remove(each));
    }
    
    private static Collection<String> findUnusedAlgorithms(final ShadowRuleConfiguration currentRuleConfig) {
        Collection<String> inUsedAlgorithms = currentRuleConfig.getTables().entrySet().stream().flatMap(entry -> entry.getValue().getShadowAlgorithmNames().stream()).collect(Collectors.toSet());
        if (null != currentRuleConfig.getDefaultShadowAlgorithmName()) {
            inUsedAlgorithms.add(currentRuleConfig.getDefaultShadowAlgorithmName());
        }
        return currentRuleConfig.getShadowAlgorithms().keySet().stream().filter(each -> !inUsedAlgorithms.contains(each)).collect(Collectors.toSet());
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getRuleConfigurationClass() {
        return ShadowRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropShadowRuleStatement.class.getName();
    }
}
