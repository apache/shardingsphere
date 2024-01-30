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
import org.apache.shardingsphere.distsql.statement.rdl.rule.type.DropRuleStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
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
        executor.setDatabase(database);
        Optional<ShardingSphereRule> rule = database.getRuleMetaData().findSingleRule(executor.getRuleClass());
        executor.setRule(rule.orElse(null));
        checkBeforeUpdate(rule.orElse(null));
        RuleConfiguration currentRuleConfig = rule.map(ShardingSphereRule::getConfiguration).orElse(null);
        if (getRefreshStatus(currentRuleConfig)) {
            contextManager.getMetaDataContexts().getPersistService().getMetaDataVersionPersistService()
                    .switchActiveVersion(DatabaseRuleOperatorFactory.newInstance(contextManager, executor).operate(sqlStatement, database, currentRuleConfig));
        }
    }
    
    @SuppressWarnings("unchecked")
    private void checkBeforeUpdate(final ShardingSphereRule rule) {
        Optional.ofNullable(executor.getClass().getAnnotation(DistSQLExecutorCurrentRuleRequired.class)).ifPresent(optional -> checkCurrentRule(rule, optional));
        executor.checkBeforeUpdate(sqlStatement);
    }
    
    private void checkCurrentRule(final ShardingSphereRule rule, final DistSQLExecutorCurrentRuleRequired currentRuleRequired) {
        if (sqlStatement instanceof DropRuleStatement && ((DropRuleStatement) sqlStatement).isIfExists()) {
            return;
        }
        ShardingSpherePreconditions.checkNotNull(rule, () -> new MissingRequiredRuleException(currentRuleRequired.value(), database.getName()));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean getRefreshStatus(final RuleConfiguration currentRuleConfig) {
        return !(executor instanceof DatabaseRuleDropExecutor) || ((DatabaseRuleDropExecutor) executor).hasAnyOneToBeDropped(sqlStatement, currentRuleConfig);
    }
}
