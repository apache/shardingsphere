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

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleCreateExecutor;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.DuplicateAlgorithmException;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.statement.CreateDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Create default shadow algorithm statement executor.
 */
@Setter
public final class CreateDefaultShadowAlgorithmExecutor implements DatabaseRuleCreateExecutor<CreateDefaultShadowAlgorithmStatement, ShadowRule, ShadowRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShadowRule rule;
    
    @Override
    public void checkBeforeUpdate(final CreateDefaultShadowAlgorithmStatement sqlStatement) {
        if (!sqlStatement.isIfNotExists()) {
            checkAlgorithmExisted();
        }
        checkAlgorithmCompleteness(sqlStatement);
        checkAlgorithmType(sqlStatement);
    }
    
    private void checkAlgorithmExisted() {
        Collection<String> duplicatedAlgorithmNames = getDuplicatedAlgorithmNames();
        ShardingSpherePreconditions.checkState(duplicatedAlgorithmNames.isEmpty(), () -> new DuplicateAlgorithmException("Shadow", database.getName(), duplicatedAlgorithmNames));
    }
    
    private Collection<String> getDuplicatedAlgorithmNames() {
        Collection<String> algorithmNames = null == rule ? Collections.emptyList() : rule.getShadowAlgorithms().keySet();
        return Stream.of("default_shadow_algorithm").filter(algorithmNames::contains).collect(Collectors.toSet());
    }
    
    private void checkAlgorithmCompleteness(final CreateDefaultShadowAlgorithmStatement sqlStatement) {
        ShardingSpherePreconditions.checkState(!sqlStatement.getShadowAlgorithmSegment().getAlgorithmSegment().getName().isEmpty(), () -> new InvalidAlgorithmConfigurationException("Shadow"));
    }
    
    private void checkAlgorithmType(final CreateDefaultShadowAlgorithmStatement sqlStatement) {
        AlgorithmSegment shadowAlgorithmType = sqlStatement.getShadowAlgorithmSegment().getAlgorithmSegment();
        TypedSPILoader.checkService(ShadowAlgorithm.class, shadowAlgorithmType.getName(), shadowAlgorithmType.getProps());
    }
    
    @Override
    public ShadowRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateDefaultShadowAlgorithmStatement sqlStatement) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        if (getDuplicatedAlgorithmNames().isEmpty()) {
            result = new ShadowRuleConfiguration();
            result.setShadowAlgorithms(buildAlgorithmMap(sqlStatement));
            result.setDefaultShadowAlgorithmName("default_shadow_algorithm");
        }
        return result;
    }
    
    private Map<String, AlgorithmConfiguration> buildAlgorithmMap(final CreateDefaultShadowAlgorithmStatement sqlStatement) {
        return Collections.singletonMap("default_shadow_algorithm",
                new AlgorithmConfiguration(sqlStatement.getShadowAlgorithmSegment().getAlgorithmSegment().getName(), sqlStatement.getShadowAlgorithmSegment().getAlgorithmSegment().getProps()));
    }
    
    @Override
    public Class<ShadowRule> getRuleClass() {
        return ShadowRule.class;
    }
    
    @Override
    public Class<CreateDefaultShadowAlgorithmStatement> getType() {
        return CreateDefaultShadowAlgorithmStatement.class;
    }
}
