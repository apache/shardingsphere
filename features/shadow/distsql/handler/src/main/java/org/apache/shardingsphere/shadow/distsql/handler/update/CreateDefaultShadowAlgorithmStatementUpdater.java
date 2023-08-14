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
import org.apache.shardingsphere.distsql.handler.exception.algorithm.DuplicateAlgorithmException;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Create default shadow algorithm statement updater.
 */
public final class CreateDefaultShadowAlgorithmStatementUpdater implements RuleDefinitionCreateUpdater<CreateDefaultShadowAlgorithmStatement, ShadowRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateDefaultShadowAlgorithmStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        if (!sqlStatement.isIfNotExists()) {
            checkExisted(database.getName(), currentRuleConfig);
        }
        checkAlgorithmCompleteness(Collections.singleton(sqlStatement.getShadowAlgorithmSegment().getAlgorithmSegment()));
        checkAlgorithmType(sqlStatement);
    }
    
    @Override
    public ShadowRuleConfiguration buildToBeCreatedRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final CreateDefaultShadowAlgorithmStatement sqlStatement) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        if (getDuplicatedRuleNames(currentRuleConfig).isEmpty()) {
            result = new ShadowRuleConfiguration();
            result.setShadowAlgorithms(buildAlgorithmMap(sqlStatement));
            result.setDefaultShadowAlgorithmName("default_shadow_algorithm");
        }
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final ShadowRuleConfiguration toBeCreatedRuleConfig) {
        currentRuleConfig.getShadowAlgorithms().putAll(toBeCreatedRuleConfig.getShadowAlgorithms());
        if (!Strings.isNullOrEmpty(toBeCreatedRuleConfig.getDefaultShadowAlgorithmName())) {
            currentRuleConfig.setDefaultShadowAlgorithmName(toBeCreatedRuleConfig.getDefaultShadowAlgorithmName());
        }
    }
    
    private Map<String, AlgorithmConfiguration> buildAlgorithmMap(final CreateDefaultShadowAlgorithmStatement sqlStatement) {
        return Collections.singletonMap("default_shadow_algorithm", new AlgorithmConfiguration(sqlStatement.getShadowAlgorithmSegment().getAlgorithmSegment().getName(),
                sqlStatement.getShadowAlgorithmSegment().getAlgorithmSegment().getProps()));
    }
    
    private Collection<String> getDuplicatedRuleNames(final ShadowRuleConfiguration currentRuleConfig) {
        Collection<String> currentAlgorithmNames = null == currentRuleConfig ? Collections.emptyList() : currentRuleConfig.getShadowAlgorithms().keySet();
        return Stream.of("default_shadow_algorithm").filter(currentAlgorithmNames::contains).collect(Collectors.toSet());
    }
    
    private void checkExisted(final String databaseName, final ShadowRuleConfiguration currentRuleConfig) {
        Collection<String> duplicatedRuleNames = getDuplicatedRuleNames(currentRuleConfig);
        ShardingSpherePreconditions.checkState(duplicatedRuleNames.isEmpty(), () -> new DuplicateAlgorithmException("shadow", databaseName, duplicatedRuleNames));
    }
    
    private void checkAlgorithmType(final CreateDefaultShadowAlgorithmStatement sqlStatement) {
        AlgorithmSegment shadowAlgorithmType = sqlStatement.getShadowAlgorithmSegment().getAlgorithmSegment();
        TypedSPILoader.checkService(ShadowAlgorithm.class, shadowAlgorithmType.getName(), shadowAlgorithmType.getProps());
    }
    
    private void checkAlgorithmCompleteness(final Collection<AlgorithmSegment> algorithmSegments) {
        Collection<AlgorithmSegment> incompleteAlgorithms = algorithmSegments.stream().filter(each -> each.getName().isEmpty()).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(incompleteAlgorithms.isEmpty(), () -> new InvalidAlgorithmConfigurationException("shadow"));
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getRuleConfigurationClass() {
        return ShadowRuleConfiguration.class;
    }
    
    @Override
    public Class<CreateDefaultShadowAlgorithmStatement> getType() {
        return CreateDefaultShadowAlgorithmStatement.class;
    }
}
