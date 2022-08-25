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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterTransactionRuleStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.UpdatableRALBackendHandler;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import java.util.Collection;
import java.util.Map;

/**
 * Alter transaction rule statement handler.
 */
public final class AlterTransactionRuleHandler extends UpdatableRALBackendHandler<AlterTransactionRuleStatement> {
    
    @Override
    protected void update(final ContextManager contextManager) {
        replaceNewRule(contextManager);
        persistNewRuleConfigurations();
    }
    
    private void replaceNewRule(final ContextManager contextManager) {
        TransactionRuleConfiguration toBeAlteredRuleConfig = createToBeAlteredRuleConfiguration();
        Collection<ShardingSphereRule> globalRules = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules();
        globalRules.stream().filter(each -> each instanceof TransactionRule).forEach(each -> ((TransactionRule) each).closeStaleResource());
        globalRules.removeIf(each -> each instanceof TransactionRule);
        Map<String, ShardingSphereDatabase> databases = contextManager.getMetaDataContexts().getMetaData().getDatabases();
        TransactionRule transactionRule = new TransactionRule(toBeAlteredRuleConfig, databases, contextManager.getInstanceContext());
        globalRules.add(transactionRule);
    }
    
    private TransactionRuleConfiguration createToBeAlteredRuleConfiguration() {
        AlterTransactionRuleStatement sqlStatement = getSqlStatement();
        return new TransactionRuleConfiguration(sqlStatement.getDefaultType(), sqlStatement.getProvider().getProviderType(), sqlStatement.getProvider().getProps());
    }
    
    private void persistNewRuleConfigurations() {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        metaDataContexts.getPersistService().getGlobalRuleService().persist(metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), true);
    }
}
