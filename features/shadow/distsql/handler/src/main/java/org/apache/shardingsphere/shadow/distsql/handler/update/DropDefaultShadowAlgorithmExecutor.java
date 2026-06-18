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
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.statement.DropDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Collections;

/**
 * Drop default shadow algorithm executor.
 */
@DistSQLExecutorCurrentRuleRequired(ShadowRule.class)
@Setter
public final class DropDefaultShadowAlgorithmExecutor implements DatabaseRuleDropExecutor<DropDefaultShadowAlgorithmStatement, ShadowRule, ShadowRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShadowRule rule;
    
    @Override
    public void checkBeforeUpdate(final DropDefaultShadowAlgorithmStatement sqlStatement) {
        if (!sqlStatement.isIfExists()) {
            checkAlgorithm();
        }
    }
    
    private void checkAlgorithm() {
        ShardingSpherePreconditions.checkNotNull(rule.getConfiguration().getDefaultShadowAlgorithmName(),
                () -> new UnregisteredAlgorithmException("Shadow", "default", new SQLExceptionIdentifier(database.getName())));
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropDefaultShadowAlgorithmStatement sqlStatement) {
        return null != rule.getConfiguration().getDefaultShadowAlgorithmName();
    }
    
    @Override
    public ShadowRuleConfiguration buildToBeDroppedRuleConfiguration(final DropDefaultShadowAlgorithmStatement sqlStatement) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(Collections.singletonMap(rule.getConfiguration().getDefaultShadowAlgorithmName(),
                rule.getConfiguration().getShadowAlgorithms().get(rule.getConfiguration().getDefaultShadowAlgorithmName())));
        result.setDefaultShadowAlgorithmName(rule.getConfiguration().getDefaultShadowAlgorithmName());
        return result;
    }
    
    @Override
    public Class<ShadowRule> getRuleClass() {
        return ShadowRule.class;
    }
    
    @Override
    public Class<DropDefaultShadowAlgorithmStatement> getType() {
        return DropDefaultShadowAlgorithmStatement.class;
    }
}
