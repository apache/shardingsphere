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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.sqlfederation.optimizer.context.OptimizerContextFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SQL federation rule.
 */
@Getter
public final class SQLFederationRule implements GlobalRule {
    
    private final SQLFederationRuleConfiguration configuration;
    
    private final AtomicReference<OptimizerContext> optimizerContext;
    
    private final RuleAttributes attributes;
    
    public SQLFederationRule(final SQLFederationRuleConfiguration ruleConfig, final Map<String, ShardingSphereDatabase> databases) {
        configuration = ruleConfig;
        optimizerContext = new AtomicReference<>(OptimizerContextFactory.create(databases));
        attributes = new RuleAttributes();
    }
    
    @Override
    public void refresh(final Map<String, ShardingSphereDatabase> databases, final GlobalRuleChangedType changedType) {
        optimizerContext.set(OptimizerContextFactory.create(databases));
    }
    
    /**
     * Get optimizer context.
     * 
     * @return optimizer context
     */
    public OptimizerContext getOptimizerContext() {
        return optimizerContext.get();
    }
}
