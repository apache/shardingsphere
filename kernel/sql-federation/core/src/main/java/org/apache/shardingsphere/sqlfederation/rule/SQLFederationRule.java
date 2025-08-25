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

package org.apache.shardingsphere.sqlfederation.rule;

import lombok.Getter;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContext;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContextFactory;
import org.apache.shardingsphere.sqlfederation.compiler.exception.InvalidExecutionPlanCacheConfigException;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.constant.SQLFederationOrder;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SQL federation rule.
 */
@Getter
public final class SQLFederationRule implements GlobalRule {
    
    private final SQLFederationRuleConfiguration configuration;
    
    private final AtomicReference<CompilerContext> compilerContext;
    
    public SQLFederationRule(final SQLFederationRuleConfiguration ruleConfig, final Collection<ShardingSphereDatabase> databases) {
        configuration = ruleConfig;
        compilerContext = new AtomicReference<>(CompilerContextFactory.create(databases));
        checkExecutionPlanCacheConfig(ruleConfig.getExecutionPlanCache());
    }
    
    private void checkExecutionPlanCacheConfig(final SQLFederationCacheOption executionPlanCache) {
        ShardingSpherePreconditions.checkState(executionPlanCache.getInitialCapacity() > 0,
                () -> new InvalidExecutionPlanCacheConfigException("initialCapacity", executionPlanCache.getInitialCapacity()));
        ShardingSpherePreconditions.checkState(executionPlanCache.getMaximumSize() > 0, () -> new InvalidExecutionPlanCacheConfigException("maximumSize", executionPlanCache.getMaximumSize()));
    }
    
    @Override
    public void refresh(final Collection<ShardingSphereDatabase> databases, final GlobalRuleChangedType changedType) {
        compilerContext.set(CompilerContextFactory.create(databases));
    }
    
    /**
     * Get compiler context.
     *
     * @return compiler context
     */
    public CompilerContext getCompilerContext() {
        return compilerContext.get();
    }
    
    @Override
    public int getOrder() {
        return SQLFederationOrder.ORDER;
    }
}
