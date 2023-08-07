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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.ral.update.GlobalRuleRALUpdater;
import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.UpdatableGlobalRuleRALStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.DistSQLBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Updatable RAL backend handler for global rule.
 */
@RequiredArgsConstructor
public final class UpdatableGlobalRuleRALBackendHandler implements DistSQLBackendHandler {
    
    private final UpdatableGlobalRuleRALStatement sqlStatement;
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ResponseHeader execute() {
        GlobalRuleRALUpdater globalRuleUpdater = TypedSPILoader.getService(GlobalRuleRALUpdater.class, sqlStatement.getClass());
        Class<? extends RuleConfiguration> ruleConfigClass = globalRuleUpdater.getRuleConfigurationClass();
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        Collection<RuleConfiguration> ruleConfigurations = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getConfigurations();
        RuleConfiguration currentRuleConfig = findCurrentRuleConfiguration(ruleConfigurations, ruleConfigClass);
        globalRuleUpdater.checkSQLStatement(currentRuleConfig, sqlStatement);
        contextManager.getInstanceContext().getModeContextManager().alterGlobalRuleConfiguration(processUpdate(ruleConfigurations, sqlStatement, globalRuleUpdater, currentRuleConfig));
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private RuleConfiguration findCurrentRuleConfiguration(final Collection<RuleConfiguration> ruleConfigurations, final Class<? extends RuleConfiguration> ruleConfigClass) {
        for (RuleConfiguration each : ruleConfigurations) {
            if (ruleConfigClass.isAssignableFrom(each.getClass())) {
                return each;
            }
        }
        throw new MissingRequiredRuleException(ruleConfigClass.getSimpleName());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Collection<RuleConfiguration> processUpdate(final Collection<RuleConfiguration> ruleConfigurations, final RALStatement sqlStatement, final GlobalRuleRALUpdater globalRuleUpdater,
                                                        final RuleConfiguration currentRuleConfig) {
        Collection<RuleConfiguration> result = new LinkedList<>(ruleConfigurations);
        result.remove(currentRuleConfig);
        result.add(globalRuleUpdater.buildAlteredRuleConfiguration(currentRuleConfig, sqlStatement));
        return result;
    }
}
