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
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.engine.database.DatabaseRuleDefinitionExecuteEngine;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.engine.global.GlobalRuleDefinitionExecuteEngine;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.engine.legacy.LegacyGlobalRuleDefinitionExecuteEngine;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.spi.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.util.DatabaseNameUtils;
import org.apache.shardingsphere.distsql.statement.rdl.rule.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.handler.distsql.legacy.LegacyDatabaseRuleDefinitionExecuteEngine;

import java.util.Optional;

/**
 *  // TODO Remove this to distsql-handler module.
 * Rule definition execute engine.
 */
@RequiredArgsConstructor
public abstract class RuleDefinitionExecuteEngine {
    
    private final RuleDefinitionStatement sqlStatement;
    
    private final String currentDatabaseName;
    
    private final ContextManager contextManager;
    
    /**
     * Execute update.
     */
    @SuppressWarnings("rawtypes")
    public void executeUpdate() {
        Optional<DatabaseRuleDefinitionExecutor> databaseExecutor = TypedSPILoader.findService(DatabaseRuleDefinitionExecutor.class, sqlStatement.getClass());
        if (databaseExecutor.isPresent()) {
            executeDatabaseRule(databaseExecutor.get());
        } else {
            executeGlobalRule(TypedSPILoader.getService(GlobalRuleDefinitionExecutor.class, sqlStatement.getClass()));
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void executeDatabaseRule(final DatabaseRuleDefinitionExecutor databaseExecutor) {
        if (isNormalRuleUpdater()) {
            new DatabaseRuleDefinitionExecuteEngine(
                    sqlStatement, contextManager, getDatabase(DatabaseNameUtils.getDatabaseName(sqlStatement, currentDatabaseName)), databaseExecutor).executeUpdate();
        } else {
            new LegacyDatabaseRuleDefinitionExecuteEngine(
                    sqlStatement, contextManager, getDatabase(DatabaseNameUtils.getDatabaseName(sqlStatement, currentDatabaseName)), databaseExecutor).executeUpdate();
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void executeGlobalRule(final GlobalRuleDefinitionExecutor globalExecutor) {
        if (isNormalRuleUpdater()) {
            new GlobalRuleDefinitionExecuteEngine(sqlStatement, contextManager, globalExecutor).executeUpdate();
        } else {
            new LegacyGlobalRuleDefinitionExecuteEngine(sqlStatement, contextManager, globalExecutor).executeUpdate();
        }
    }
    
    private boolean isNormalRuleUpdater() {
        String modeType = contextManager.getInstanceContext().getModeConfiguration().getType();
        return "Cluster".equals(modeType) || "Standalone".equals(modeType);
    }
    
    protected abstract ShardingSphereDatabase getDatabase(String databaseName);
}
