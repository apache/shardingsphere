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

import org.apache.shardingsphere.infra.exception.core.external.sql.type.kernel.category.DistSQLException;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.AlgorithmInUsedException;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.handler.supporter.ShadowRuleStatementSupporter;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowAlgorithmStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Drop shadow algorithm statement updater.
 */
public final class DropShadowAlgorithmStatementUpdater implements RuleDefinitionDropUpdater<DropShadowAlgorithmStatement, ShadowRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final DropShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        if (sqlStatement.isIfExists() && !isExistRuleConfig(currentRuleConfig)) {
            return;
        }
        ShadowRuleStatementChecker.checkRuleConfigurationExists(database.getName(), currentRuleConfig);
        checkAlgorithm(database.getName(), sqlStatement, currentRuleConfig);
    }
    
    private void checkAlgorithm(final String databaseName, final DropShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        Collection<String> currentAlgorithms = ShadowRuleStatementSupporter.getAlgorithmNames(currentRuleConfig);
        Collection<String> requiredAlgorithms = sqlStatement.getNames();
        String defaultShadowAlgorithmName = currentRuleConfig.getDefaultShadowAlgorithmName();
        if (!sqlStatement.isIfExists()) {
            ShadowRuleStatementChecker.checkExisted(requiredAlgorithms, currentAlgorithms, notExistedAlgorithms -> new MissingRequiredAlgorithmException("shadow", databaseName, notExistedAlgorithms));
        }
        checkAlgorithmInUsed(requiredAlgorithms, getAlgorithmInUse(currentRuleConfig), identical -> new AlgorithmInUsedException("Sharding", databaseName, identical));
        ShardingSpherePreconditions.checkState(!requiredAlgorithms.contains(defaultShadowAlgorithmName),
                () -> new AlgorithmInUsedException("Shadow", databaseName, Collections.singleton(defaultShadowAlgorithmName)));
    }
    
    private void checkAlgorithmInUsed(final Collection<String> requiredAlgorithms, final Collection<String> currentAlgorithms, final Function<Collection<String>, DistSQLException> thrower) {
        ShadowRuleStatementChecker.checkDuplicated(requiredAlgorithms, currentAlgorithms, thrower);
    }
    
    private Collection<String> getAlgorithmInUse(final ShadowRuleConfiguration currentRuleConfig) {
        return currentRuleConfig.getTables().values().stream().filter(each -> !each.getDataSourceNames().isEmpty()).map(ShadowTableConfiguration::getShadowAlgorithmNames)
                .flatMap(Collection::stream).collect(Collectors.toSet());
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        return null != currentRuleConfig
                && !getIdenticalData(ShadowRuleStatementSupporter.getAlgorithmNames(currentRuleConfig), sqlStatement.getNames()).isEmpty();
    }
    
    @Override
    public ShadowRuleConfiguration buildToBeDroppedRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final DropShadowAlgorithmStatement sqlStatement) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        for (String each : sqlStatement.getNames()) {
            result.getShadowAlgorithms().put(each, currentRuleConfig.getShadowAlgorithms().get(each));
        }
        return result;
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        Collection<String> algorithmNames = sqlStatement.getNames();
        algorithmNames.forEach(each -> currentRuleConfig.getShadowAlgorithms().remove(each));
        currentRuleConfig.getTables().forEach((key, value) -> value.getShadowAlgorithmNames().removeIf(algorithmNames::contains));
        getEmptyTableRules(currentRuleConfig.getTables()).forEach(each -> currentRuleConfig.getTables().remove(each));
        return false;
    }
    
    private Collection<String> getEmptyTableRules(final Map<String, ShadowTableConfiguration> tables) {
        return tables.entrySet().stream().filter(entry -> entry.getValue().getShadowAlgorithmNames().isEmpty() && entry.getValue().getDataSourceNames().isEmpty())
                .map(Entry::getKey).collect(Collectors.toSet());
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getRuleConfigurationClass() {
        return ShadowRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropShadowAlgorithmStatement.class.getName();
    }
}
