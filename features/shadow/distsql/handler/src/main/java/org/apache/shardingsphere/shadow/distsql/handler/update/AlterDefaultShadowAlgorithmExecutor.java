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
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleAlterExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.statement.AlterDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collections;
import java.util.Map;

/**
 * Alter default shadow algorithm executor.
 */
@DistSQLExecutorCurrentRuleRequired(ShadowRule.class)
@Setter
public final class AlterDefaultShadowAlgorithmExecutor implements DatabaseRuleAlterExecutor<AlterDefaultShadowAlgorithmStatement, ShadowRule, ShadowRuleConfiguration> {
    
    private static final String DEFAULT_ALGORITHM_NAME = "default_shadow_algorithm";
    
    private ShardingSphereDatabase database;
    
    private ShadowRule rule;
    
    @Override
    public void checkBeforeUpdate(final AlterDefaultShadowAlgorithmStatement sqlStatement) {
        checkAlgorithms(sqlStatement.getShadowAlgorithmSegment().getAlgorithmSegment());
    }
    
    private void checkAlgorithms(final AlgorithmSegment algorithmSegment) {
        checkAlgorithmCompleteness(algorithmSegment);
        checkAlgorithmType(algorithmSegment);
        ShadowRuleStatementChecker.checkExisted(Collections.singleton(DEFAULT_ALGORITHM_NAME), rule.getConfiguration().getShadowAlgorithms().keySet(),
                notExistedAlgorithms -> new UnregisteredAlgorithmException("Shadow", notExistedAlgorithms, new SQLExceptionIdentifier(database.getName())));
    }
    
    private void checkAlgorithmCompleteness(final AlgorithmSegment algorithmSegment) {
        ShardingSpherePreconditions.checkNotEmpty(algorithmSegment.getName(), () -> new MissingRequiredAlgorithmException("Shadow", new SQLExceptionIdentifier("")));
    }
    
    private void checkAlgorithmType(final AlgorithmSegment algorithmSegment) {
        TypedSPILoader.checkService(ShadowAlgorithm.class, algorithmSegment.getName(), algorithmSegment.getProps());
    }
    
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
    public ShadowRuleConfiguration buildToBeDroppedRuleConfiguration(final ShadowRuleConfiguration toBeAlteredRuleConfig) {
        return null;
    }
    
    @Override
    public Class<ShadowRule> getRuleClass() {
        return ShadowRule.class;
    }
    
    @Override
    public Class<AlterDefaultShadowAlgorithmStatement> getType() {
        return AlterDefaultShadowAlgorithmStatement.class;
    }
}
