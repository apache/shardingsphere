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

import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.AlgorithmInUsedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredAlgorithmMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowAlgorithmStatement;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Drop shadow algorithm statement updater.
 */
public final class DropShadowAlgorithmStatementUpdater implements RuleDefinitionDropUpdater<DropShadowAlgorithmStatement, ShadowRuleConfiguration> {
    
    private static final String SHADOW = "shadow";
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData metaData, final DropShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = metaData.getName();
        checkConfigurationExist(schemaName, currentRuleConfig);
        checkAlgorithm(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkAlgorithm(final String schemaName, final DropShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> currentAlgorithms = getCurrentAlgorithms(currentRuleConfig);
        Collection<String> requireAlgorithms = sqlStatement.getAlgorithmNames();
        checkDifferent(requireAlgorithms, currentAlgorithms, different -> new RequiredAlgorithmMissedException(SHADOW, schemaName, different));
        checkIdentical(requireAlgorithms, getAlgorithmInUse(currentRuleConfig), identical -> new AlgorithmInUsedException(schemaName, identical));
    }
    
    private Set<String> getAlgorithmInUse(final ShadowRuleConfiguration currentRuleConfig) {
        return currentRuleConfig.getTables().values().stream().map(ShadowTableConfiguration::getShadowAlgorithmNames).flatMap(Collection::stream).collect(Collectors.toSet());
    }
    
    private void checkConfigurationExist(final String schemaName, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        DistSQLException.predictionThrow(currentRuleConfig != null, new RequiredRuleMissedException(SHADOW, schemaName));
    }
    
    private void checkIdentical(final Collection<String> require, final Collection<String> current, final Function<Set<String>, DistSQLException> thrower) throws DistSQLException {
        Set<String> identical = getIdentical(require, current);
        DistSQLException.predictionThrow(identical.isEmpty(), thrower.apply(identical));
    }
    
    private void checkDifferent(final Collection<String> require, final Collection<String> current, final Function<Set<String>, DistSQLException> thrower) throws DistSQLException {
        Set<String> duplicateRequire = getDifferent(require, current);
        DistSQLException.predictionThrow(duplicateRequire.isEmpty(), thrower.apply(duplicateRequire));
    }
    
    private Set<String> getDifferent(final Collection<String> require, final Collection<String> current) {
        return require.stream().filter(each -> !current.contains(each)).collect(Collectors.toSet());
    }
    
    private Set<String> getIdentical(final Collection<String> require, final Collection<String> current) {
        return require.stream().filter(current::contains).collect(Collectors.toSet());
    }
    
    private Collection<String> getCurrentAlgorithms(final ShadowRuleConfiguration configuration) {
        return configuration.getShadowAlgorithms().keySet();
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        sqlStatement.getAlgorithmNames().forEach(each -> {
            getCurrentAlgorithms(currentRuleConfig).removeIf(each::equalsIgnoreCase);
        });
        return false;
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getRuleConfigurationClass() {
        return ShadowRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropShadowAlgorithmStatement.class.getCanonicalName();
    }
}
