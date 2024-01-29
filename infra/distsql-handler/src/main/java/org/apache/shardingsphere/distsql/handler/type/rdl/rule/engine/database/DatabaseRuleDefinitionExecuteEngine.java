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

package org.apache.shardingsphere.distsql.handler.type.rdl.rule.engine.database;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.spi.database.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.statement.rdl.rule.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Optional;

/**
 * Database rule definition execute engine.
 */
@RequiredArgsConstructor
public final class DatabaseRuleDefinitionExecuteEngine {
    
    private final RuleDefinitionStatement sqlStatement;
    
    private final ContextManager contextManager;
    
    private final ShardingSphereDatabase database;
    
    @SuppressWarnings("rawtypes")
    private final DatabaseRuleDefinitionExecutor executor;
    
    /**
     * Execute update.
     */
    @SuppressWarnings("unchecked")
    public void executeUpdate() {
        Class<? extends RuleConfiguration> ruleConfigClass = executor.getRuleConfigurationClass();
        RuleConfiguration currentRuleConfig = findCurrentRuleConfiguration(database, ruleConfigClass).orElse(null);
        executor.setDatabase(database);
        checkBeforeUpdate(currentRuleConfig);
        if (getRefreshStatus(currentRuleConfig)) {
            contextManager.getMetaDataContexts().getPersistService().getMetaDataVersionPersistService()
                    .switchActiveVersion(DatabaseRuleOperatorFactory.newInstance(contextManager, executor).operate(sqlStatement, database, currentRuleConfig));
        }
    }
    
    @SuppressWarnings("unchecked")
    private void checkBeforeUpdate(final RuleConfiguration currentRuleConfig) {
        DistSQLExecutorCurrentRuleRequired currentRuleRequired = executor.getClass().getAnnotation(DistSQLExecutorCurrentRuleRequired.class);
        if (null != currentRuleRequired) {
            ShardingSpherePreconditions.checkNotNull(currentRuleConfig, () -> new MissingRequiredRuleException(currentRuleRequired.value(), database.getName()));
        }
        executor.checkBeforeUpdate(sqlStatement, currentRuleConfig);
    }
    
    private Optional<RuleConfiguration> findCurrentRuleConfiguration(final ShardingSphereDatabase database, final Class<? extends RuleConfiguration> ruleConfigClass) {
        return database.getRuleMetaData().getConfigurations().stream().filter(each -> ruleConfigClass.isAssignableFrom(each.getClass())).findFirst();
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean getRefreshStatus(final RuleConfiguration currentRuleConfig) {
        return !(executor instanceof DatabaseRuleDropExecutor) || ((DatabaseRuleDropExecutor) executor).hasAnyOneToBeDropped(sqlStatement, currentRuleConfig);
    }
}
