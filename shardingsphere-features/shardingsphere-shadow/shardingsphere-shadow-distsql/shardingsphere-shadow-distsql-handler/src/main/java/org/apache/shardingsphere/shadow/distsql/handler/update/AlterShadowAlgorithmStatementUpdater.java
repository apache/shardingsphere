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

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.AlgorithmInUsedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.AlterShadowAlgorithmStatement;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Alter shadow algorithm statement updater.
 */
public final class AlterShadowAlgorithmStatementUpdater implements RuleDefinitionAlterUpdater<AlterShadowAlgorithmStatement, ShadowRuleConfiguration> {
    
    private static final String SHADOW = "shadow";
    
    @Override
    public RuleConfiguration buildToBeAlteredRuleConfiguration(final AlterShadowAlgorithmStatement sqlStatement) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(buildAlgorithmMap(sqlStatement));
        return result;
    }
    
    private Map<String, AlgorithmConfiguration> buildAlgorithmMap(final AlterShadowAlgorithmStatement sqlStatement) {
        return sqlStatement.getAlgorithms().stream().collect(Collectors.toMap(ShadowAlgorithmSegment::getAlgorithmName, each -> new AlgorithmConfiguration(
                each.getAlgorithmSegment().getName(), each.getAlgorithmSegment().getProps())));
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final ShadowRuleConfiguration toBeAlteredRuleConfig) {
        currentRuleConfig.getShadowAlgorithms().putAll(toBeAlteredRuleConfig.getShadowAlgorithms());
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final AlterShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        checkConfigurationExist(database.getName(), currentRuleConfig);
        checkAlgorithms(database.getName(), sqlStatement, currentRuleConfig);
    }
    
    private void checkConfigurationExist(final String databaseName, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        ShadowRuleStatementChecker.checkConfigurationExist(databaseName, currentRuleConfig);
    }
    
    private void checkAlgorithms(final String databaseName, final AlterShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        ShadowRuleStatementChecker.checkAlgorithmCompleteness(sqlStatement.getAlgorithms());
        Collection<String> requireAlgorithmNames = sqlStatement.getAlgorithms().stream().map(ShadowAlgorithmSegment::getAlgorithmName).collect(Collectors.toList());
        ShadowRuleStatementChecker.checkAnyDuplicate(requireAlgorithmNames, duplicated -> new AlgorithmInUsedException("Shadow", databaseName, duplicated));
        ShadowRuleStatementChecker.checkAlgorithmExist(requireAlgorithmNames, currentRuleConfig.getShadowAlgorithms().keySet(), different -> new MissingRequiredAlgorithmException(
                SHADOW, databaseName, different));
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getRuleConfigurationClass() {
        return ShadowRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterShadowAlgorithmStatement.class.getName();
    }
}
