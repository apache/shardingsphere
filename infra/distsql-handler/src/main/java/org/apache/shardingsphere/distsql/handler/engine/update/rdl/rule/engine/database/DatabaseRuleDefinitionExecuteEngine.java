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

package org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.database;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorRequiredChecker;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.database.DatabaseRuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Database rule definition execute engine.
 */
@RequiredArgsConstructor
public final class DatabaseRuleDefinitionExecuteEngine {
    
    private final DatabaseRuleDefinitionStatement sqlStatement;
    
    private final ContextManager contextManager;
    
    private final String databaseName;
    
    @SuppressWarnings("rawtypes")
    private final DatabaseRuleDefinitionExecutor executor;
    
    /**
     * Execute update.
     * @throws SQLException SQL Exception
     */
    @SuppressWarnings("unchecked")
    public void executeUpdate() throws SQLException {
        executor.setDatabase(contextManager.getDatabase(databaseName));
        Optional<ShardingSphereRule> rule = contextManager.getDatabase(databaseName).getRuleMetaData().findSingleRule(executor.getRuleClass());
        executor.setRule(rule.orElse(null));
        checkBeforeUpdate();
        RuleConfiguration currentRuleConfig = rule.map(ShardingSphereRule::getConfiguration).orElse(null);
        if (getRefreshStatus(rule.isPresent())) {
            DatabaseRuleOperatorFactory.newInstance(contextManager, executor).operate(sqlStatement, contextManager.getDatabase(databaseName), currentRuleConfig);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void checkBeforeUpdate() {
        new DistSQLExecutorRequiredChecker(executor).check(sqlStatement, contextManager, contextManager.getDatabase(databaseName));
        executor.checkBeforeUpdate(sqlStatement);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean getRefreshStatus(final boolean currentRuleExists) {
        return !(executor instanceof DatabaseRuleDropExecutor) || currentRuleExists && ((DatabaseRuleDropExecutor) executor).hasAnyOneToBeDropped(sqlStatement);
    }
}
