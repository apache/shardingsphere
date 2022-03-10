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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.AlterTransactionRuleStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.UpdatableRALBackendHandler;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Alter transaction rule statement handler.
 */
public final class AlterTransactionRuleHandler extends UpdatableRALBackendHandler<AlterTransactionRuleStatement, AlterTrafficRuleHandler> {
    
    @Override
    protected void update(final ContextManager contextManager, final AlterTransactionRuleStatement sqlStatement) {
        updateTransactionRule();
    }
    
    private void updateTransactionRule() {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        ShardingSphereRuleMetaData globalRuleMetaData = metaDataContexts.getGlobalRuleMetaData();
        Collection<ShardingSphereRule> globalRules = globalRuleMetaData.getRules();
        globalRules.removeIf(each -> each instanceof TransactionRule);
        Collection<RuleConfiguration> globalRuleConfigurations = new LinkedList<>(globalRuleMetaData.getConfigurations());
        globalRuleConfigurations.removeIf(each -> each instanceof TransactionRuleConfiguration);
        TransactionRuleConfiguration toBeAlteredRuleConfig = buildTransactionRuleConfiguration();
        globalRules.add(new TransactionRule(toBeAlteredRuleConfig));
        globalRuleConfigurations.add(toBeAlteredRuleConfig);
        ProxyContext.getInstance().getContextManager().renewAllTransactionContext();
        Optional<MetaDataPersistService> metaDataPersistService = metaDataContexts.getMetaDataPersistService();
        if (metaDataPersistService.isPresent() && null != metaDataPersistService.get().getGlobalRuleService()) {
            metaDataPersistService.get().getGlobalRuleService().persist(globalRuleConfigurations, true);
        }
    }
    
    private TransactionRuleConfiguration buildTransactionRuleConfiguration() {
        return new TransactionRuleConfiguration(sqlStatement.getDefaultType(), sqlStatement.getProvider().getProviderType(), sqlStatement.getProvider().getProps());
    }
}
