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
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleCreateExecutor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.InUsedAlgorithmException;
import org.apache.shardingsphere.infra.algorithm.core.exception.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.statement.CreateDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collections;
import java.util.Map;

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
        boolean isDuplicatedAlgorithmName = null != rule && rule.containsShadowAlgorithm("default_shadow_algorithm");
        ShardingSpherePreconditions.checkState(!isDuplicatedAlgorithmName, () -> new InUsedAlgorithmException("Shadow", database.getName(), Collections.singleton("default_shadow_algorithm")));
    }
    
    private void checkAlgorithmCompleteness(final CreateDefaultShadowAlgorithmStatement sqlStatement) {
        ShardingSpherePreconditions.checkNotEmpty(sqlStatement.getShadowAlgorithmSegment().getAlgorithmSegment().getName(),
                () -> new MissingRequiredAlgorithmException("Shadow", new SQLExceptionIdentifier(database.getName())));
    }
    
    private void checkAlgorithmType(final CreateDefaultShadowAlgorithmStatement sqlStatement) {
        AlgorithmSegment shadowAlgorithmType = sqlStatement.getShadowAlgorithmSegment().getAlgorithmSegment();
        TypedSPILoader.checkService(ShadowAlgorithm.class, shadowAlgorithmType.getName(), shadowAlgorithmType.getProps());
    }
    
    @Override
    public ShadowRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateDefaultShadowAlgorithmStatement sqlStatement) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        if (null == rule || !rule.containsShadowAlgorithm("default_shadow_algorithm")) {
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
