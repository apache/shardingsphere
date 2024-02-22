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

package org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.global;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.statement.rdl.rule.global.GlobalRuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collection;

/**
 * Global rule definition execute engine.
 */
@RequiredArgsConstructor
public final class GlobalRuleDefinitionExecuteEngine {
    
    private final GlobalRuleDefinitionStatement sqlStatement;
    
    private final ContextManager contextManager;
    
    @SuppressWarnings("rawtypes")
    private final GlobalRuleDefinitionExecutor executor;
    
    /**
     * Execute update.
     */
    @SuppressWarnings("unchecked")
    public void executeUpdate() {
        ShardingSphereRule rule = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(executor.getRuleClass());
        executor.setRule(rule);
        executor.checkBeforeUpdate(sqlStatement);
        contextManager.getInstanceContext().getModeContextManager().alterGlobalRuleConfiguration(processUpdate(sqlStatement, rule));
    }
    
    @SuppressWarnings("unchecked")
    private RuleConfiguration processUpdate(final GlobalRuleDefinitionStatement sqlStatement, final ShardingSphereRule rule) {
        RuleConfiguration result = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        Collection<RuleConfiguration> ruleConfigs = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getConfigurations();
        ruleConfigs.remove(rule.getConfiguration());
        ruleConfigs.add(result);
        return result;
    }
}
