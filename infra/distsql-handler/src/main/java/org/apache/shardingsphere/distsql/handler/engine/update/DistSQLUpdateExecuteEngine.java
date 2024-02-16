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

package org.apache.shardingsphere.distsql.handler.engine.update;

import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorAwareSetter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.database.DatabaseRuleDefinitionExecuteEngine;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.global.GlobalRuleDefinitionExecuteEngine;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.legacy.LegacyDatabaseRuleDefinitionExecuteEngine;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.legacy.LegacyGlobalRuleDefinitionExecuteEngine;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorRequiredChecker;
import org.apache.shardingsphere.distsql.handler.util.DatabaseNameUtils;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.distsql.statement.rdl.rule.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.SQLException;
import java.util.Optional;

/**
 * DistSQL update execute engine.
 */
public final class DistSQLUpdateExecuteEngine {
    
    private final DistSQLStatement sqlStatement;
    
    private final ContextManager contextManager;
    
    private final String databaseName;
    
    public DistSQLUpdateExecuteEngine(final DistSQLStatement sqlStatement, final String currentDatabaseName, final ContextManager contextManager) {
        this.sqlStatement = sqlStatement;
        this.contextManager = contextManager;
        databaseName = DatabaseNameUtils.getDatabaseName(sqlStatement, currentDatabaseName);
    }
    
    /**
     * Execute update.
     * 
     * @throws SQLException SQL exception
     */
    public void executeUpdate() throws SQLException {
        if (sqlStatement instanceof RuleDefinitionStatement) {
            executeRuleDefinitionUpdate();
        } else {
            executeNormalUpdate();
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void executeRuleDefinitionUpdate() {
        Optional<DatabaseRuleDefinitionExecutor> databaseExecutor = TypedSPILoader.findService(DatabaseRuleDefinitionExecutor.class, sqlStatement.getClass());
        if (databaseExecutor.isPresent()) {
            executeDatabaseRuleDefinitionUpdate(databaseExecutor.get());
        } else {
            executeGlobalRuleDefinitionUpdate(TypedSPILoader.getService(GlobalRuleDefinitionExecutor.class, sqlStatement.getClass()));
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void executeDatabaseRuleDefinitionUpdate(final DatabaseRuleDefinitionExecutor executor) {
        if (isNormalRuleUpdater()) {
            new DatabaseRuleDefinitionExecuteEngine((RuleDefinitionStatement) sqlStatement, contextManager, contextManager.getDatabase(databaseName), executor).executeUpdate();
        } else {
            // TODO Remove when metadata structure adjustment completed. #25485
            new LegacyDatabaseRuleDefinitionExecuteEngine((RuleDefinitionStatement) sqlStatement, contextManager, contextManager.getDatabase(databaseName), executor).executeUpdate();
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void executeGlobalRuleDefinitionUpdate(final GlobalRuleDefinitionExecutor executor) {
        if (isNormalRuleUpdater()) {
            new GlobalRuleDefinitionExecuteEngine((RuleDefinitionStatement) sqlStatement, contextManager, executor).executeUpdate();
        } else {
            // TODO Remove when metadata structure adjustment completed. #25485
            new LegacyGlobalRuleDefinitionExecuteEngine((RuleDefinitionStatement) sqlStatement, contextManager, executor).executeUpdate();
        }
    }
    
    // TODO Remove when metadata structure adjustment completed. #25485
    private boolean isNormalRuleUpdater() {
        String modeType = contextManager.getInstanceContext().getModeConfiguration().getType();
        return "Cluster".equals(modeType) || "Standalone".equals(modeType);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void executeNormalUpdate() throws SQLException {
        DistSQLUpdateExecutor executor = TypedSPILoader.getService(DistSQLUpdateExecutor.class, sqlStatement.getClass());
        ShardingSphereDatabase database = null == databaseName ? null : contextManager.getDatabase(databaseName);
        new DistSQLExecutorAwareSetter(executor).set(contextManager, database, null);
        new DistSQLExecutorRequiredChecker(executor).check(sqlStatement, contextManager, database);
        executor.executeUpdate(sqlStatement, contextManager);
    }
}
