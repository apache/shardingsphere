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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.database;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.database.DatabaseRuleRDLDropExecutor;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.database.DatabaseRuleRDLExecutor;
import org.apache.shardingsphere.distsql.statement.rdl.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.database.execute.DatabaseRuleRDLExecuteEngineFactory;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.DatabaseNameUtils;

import java.util.Optional;

/**
 * Database rule updater.
 */
@RequiredArgsConstructor
public final class DatabaseRuleUpdater {
    
    private final RuleDefinitionStatement sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    @SuppressWarnings("rawtypes")
    private final DatabaseRuleRDLExecutor executor;
    
    /**
     * Execute update.
     */
    @SuppressWarnings("unchecked")
    public void executeUpdate() {
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(DatabaseNameUtils.getDatabaseName(sqlStatement, connectionSession));
        Class<? extends RuleConfiguration> ruleConfigClass = executor.getRuleConfigurationClass();
        RuleConfiguration currentRuleConfig = findCurrentRuleConfiguration(database, ruleConfigClass).orElse(null);
        executor.checkSQLStatement(database, sqlStatement, currentRuleConfig);
        if (getRefreshStatus(currentRuleConfig)) {
            ProxyContext.getInstance().getContextManager().getMetaDataContexts().getPersistService().getMetaDataVersionPersistService()
                    .switchActiveVersion(DatabaseRuleRDLExecuteEngineFactory.newInstance(executor).execute(sqlStatement, database, currentRuleConfig));
        }
    }
    
    private Optional<RuleConfiguration> findCurrentRuleConfiguration(final ShardingSphereDatabase database, final Class<? extends RuleConfiguration> ruleConfigClass) {
        return database.getRuleMetaData().getConfigurations().stream().filter(each -> ruleConfigClass.isAssignableFrom(each.getClass())).findFirst();
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean getRefreshStatus(final RuleConfiguration currentRuleConfig) {
        return !(executor instanceof DatabaseRuleRDLDropExecutor) || ((DatabaseRuleRDLDropExecutor) executor).hasAnyOneToBeDropped(sqlStatement, currentRuleConfig);
    }
}
