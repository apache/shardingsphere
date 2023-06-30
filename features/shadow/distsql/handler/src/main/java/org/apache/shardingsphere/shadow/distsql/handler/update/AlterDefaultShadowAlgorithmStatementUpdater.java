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

import com.google.common.base.Strings;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.parser.statement.AlterDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Alter default shadow algorithm statement updater.
 */
public final class AlterDefaultShadowAlgorithmStatementUpdater implements RuleDefinitionAlterUpdater<AlterDefaultShadowAlgorithmStatement, ShadowRuleConfiguration> {
    
    private static final String DEFAULT_ALGORITHM_NAME = "default_shadow_algorithm";
    
    @Override
    public ShadowRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterDefaultShadowAlgorithmStatement sqlStatement) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(buildAlgorithmMap(sqlStatement));
        result.setDefaultShadowAlgorithmName(DEFAULT_ALGORITHM_NAME);
        return result;
    }
    
    private Map<String, AlgorithmConfiguration> buildAlgorithmMap(final AlterDefaultShadowAlgorithmStatement sqlStatement) {
        return Collections.singletonMap(DEFAULT_ALGORITHM_NAME,
                new AlgorithmConfiguration(sqlStatement.getShadowAlgorithmSegment().getAlgorithmSegment().getName(), sqlStatement.getShadowAlgorithmSegment().getAlgorithmSegment().getProps()));
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final ShadowRuleConfiguration toBeAlteredRuleConfig) {
        currentRuleConfig.getShadowAlgorithms().putAll(toBeAlteredRuleConfig.getShadowAlgorithms());
        currentRuleConfig.setDefaultShadowAlgorithmName(toBeAlteredRuleConfig.getDefaultShadowAlgorithmName());
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final AlterDefaultShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        ShadowRuleStatementChecker.checkRuleConfigurationExists(database.getName(), currentRuleConfig);
        checkAlgorithms(database.getName(), sqlStatement.getShadowAlgorithmSegment().getAlgorithmSegment(), currentRuleConfig);
    }
    
    private void checkAlgorithms(final String databaseName, final AlgorithmSegment algorithmSegment, final ShadowRuleConfiguration currentRuleConfig) {
        checkAlgorithmCompleteness(algorithmSegment);
        checkAlgorithmType(algorithmSegment);
        Collection<String> requiredAlgorithmNames = Collections.singleton(DEFAULT_ALGORITHM_NAME);
        ShadowRuleStatementChecker.checkExisted(requiredAlgorithmNames,
                currentRuleConfig.getShadowAlgorithms().keySet(), notExistedAlgorithms -> new MissingRequiredAlgorithmException("shadow", databaseName, notExistedAlgorithms));
    }
    
    private void checkAlgorithmCompleteness(final AlgorithmSegment algorithmSegment) {
        boolean isCompleteAlgorithm = !Strings.isNullOrEmpty(algorithmSegment.getName());
        ShardingSpherePreconditions.checkState(isCompleteAlgorithm, () -> new InvalidAlgorithmConfigurationException("shadow"));
    }
    
    private void checkAlgorithmType(final AlgorithmSegment algorithmSegment) {
        TypedSPILoader.checkService(ShadowAlgorithm.class, algorithmSegment.getName(), algorithmSegment.getProps());
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getRuleConfigurationClass() {
        return ShadowRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterDefaultShadowAlgorithmStatement.class.getName();
    }
}
