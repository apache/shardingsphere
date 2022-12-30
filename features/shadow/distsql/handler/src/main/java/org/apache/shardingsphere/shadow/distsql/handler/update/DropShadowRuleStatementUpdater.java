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
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowRuleStatement;

import java.util.Collection;
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
    public boolean updateCurrentRuleConfiguration(final DropShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getDataSources().removeIf(each -> sqlStatement.getNames().contains(each.getName()));
        currentRuleConfig.getTables().forEach((key, value) -> value.getDataSourceNames().removeIf(sqlStatement.getNames()::contains));
        currentRuleConfig.getTables().entrySet().removeIf(entry -> entry.getValue().getDataSourceNames().isEmpty());
        dropUnusedAlgorithm(currentRuleConfig);
        return currentRuleConfig.getDataSources().isEmpty() || currentRuleConfig.getTables().isEmpty();
    }
    
    private void dropUnusedAlgorithm(final ShadowRuleConfiguration currentRuleConfig) {
        Collection<String> inUsedAlgorithms = currentRuleConfig.getTables().entrySet().stream().flatMap(entry -> entry.getValue().getShadowAlgorithmNames().stream()).collect(Collectors.toSet());
        if (null != currentRuleConfig.getDefaultShadowAlgorithmName()) {
            inUsedAlgorithms.add(currentRuleConfig.getDefaultShadowAlgorithmName());
        }
        Collection<String> unusedAlgorithms = currentRuleConfig.getShadowAlgorithms().keySet().stream().filter(each -> !inUsedAlgorithms.contains(each)).collect(Collectors.toSet());
        unusedAlgorithms.forEach(each -> currentRuleConfig.getShadowAlgorithms().remove(each));
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
