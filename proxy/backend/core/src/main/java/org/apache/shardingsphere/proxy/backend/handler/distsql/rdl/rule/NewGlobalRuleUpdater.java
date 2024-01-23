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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.type.rdl.global.GlobalRuleRDLExecutor;
import org.apache.shardingsphere.distsql.statement.rdl.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.util.Collection;

/**
 * Global rule updater.
 */
@RequiredArgsConstructor
public final class NewGlobalRuleUpdater {
    
    private final RuleDefinitionStatement sqlStatement;
    
    @SuppressWarnings("rawtypes")
    private final GlobalRuleRDLExecutor executor;
    
    /**
     * Execute update.
     */
    @SuppressWarnings("unchecked")
    public void executeUpdate() {
        Class<? extends RuleConfiguration> ruleConfigClass = executor.getRuleConfigurationClass();
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        Collection<RuleConfiguration> ruleConfigs = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getConfigurations();
        RuleConfiguration currentRuleConfig = findCurrentRuleConfiguration(ruleConfigs, ruleConfigClass);
        executor.checkSQLStatement(currentRuleConfig, sqlStatement);
        contextManager.getInstanceContext().getModeContextManager().alterGlobalRuleConfiguration(processUpdate(ruleConfigs, sqlStatement, executor, currentRuleConfig));
    }
    
    private RuleConfiguration findCurrentRuleConfiguration(final Collection<RuleConfiguration> ruleConfigs, final Class<? extends RuleConfiguration> ruleConfigClass) {
        for (RuleConfiguration each : ruleConfigs) {
            if (ruleConfigClass.isAssignableFrom(each.getClass())) {
                return each;
            }
        }
        throw new MissingRequiredRuleException(ruleConfigClass.getSimpleName());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private RuleConfiguration processUpdate(final Collection<RuleConfiguration> ruleConfigurations, final RuleDefinitionStatement sqlStatement, final GlobalRuleRDLExecutor executor,
                                            final RuleConfiguration currentRuleConfig) {
        RuleConfiguration result = executor.buildAlteredRuleConfiguration(currentRuleConfig, sqlStatement);
        ruleConfigurations.remove(currentRuleConfig);
        ruleConfigurations.add(result);
        return result;
    }
}
