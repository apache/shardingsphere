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

package org.apache.shardingsphere.distsql.handler.type.update;

import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorClusterModeRequired;
import org.apache.shardingsphere.distsql.handler.type.update.rdl.rule.engine.database.DatabaseRuleDefinitionExecuteEngine;
import org.apache.shardingsphere.distsql.handler.type.update.rdl.rule.engine.global.GlobalRuleDefinitionExecuteEngine;
import org.apache.shardingsphere.distsql.handler.type.update.rdl.rule.engine.legacy.LegacyDatabaseRuleDefinitionExecuteEngine;
import org.apache.shardingsphere.distsql.handler.type.update.rdl.rule.engine.legacy.LegacyGlobalRuleDefinitionExecuteEngine;
import org.apache.shardingsphere.distsql.handler.type.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.type.update.rdl.rule.spi.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.util.DatabaseNameUtils;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.distsql.statement.rdl.rule.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.SQLException;
import java.util.Optional;

/**
 * DistSQL update execute engine.
 */
public abstract class DistSQLUpdateExecuteEngine {
    
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
    private void executeDatabaseRuleDefinitionUpdate(final DatabaseRuleDefinitionExecutor databaseExecutor) {
        if (isNormalRuleUpdater()) {
            new DatabaseRuleDefinitionExecuteEngine(
                    (RuleDefinitionStatement) sqlStatement, contextManager, getDatabase(databaseName), databaseExecutor).executeUpdate();
        } else {
            // TODO Remove when metadata structure adjustment completed. #25485 
            new LegacyDatabaseRuleDefinitionExecuteEngine(
                    (RuleDefinitionStatement) sqlStatement, contextManager, getDatabase(databaseName), databaseExecutor).executeUpdate();
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void executeGlobalRuleDefinitionUpdate(final GlobalRuleDefinitionExecutor globalExecutor) {
        if (isNormalRuleUpdater()) {
            new GlobalRuleDefinitionExecuteEngine((RuleDefinitionStatement) sqlStatement, contextManager, globalExecutor).executeUpdate();
        } else {
            // TODO Remove when metadata structure adjustment completed. #25485
            new LegacyGlobalRuleDefinitionExecuteEngine((RuleDefinitionStatement) sqlStatement, contextManager, globalExecutor).executeUpdate();
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
        if (executor instanceof DistSQLExecutorDatabaseAware) {
            ((DistSQLExecutorDatabaseAware) executor).setDatabase(getDatabase(databaseName));
        }
        if (executor instanceof DistSQLExecutorRuleAware) {
            setRule((DistSQLExecutorRuleAware) executor);
        }
        checkBeforeUpdate(executor);
        executor.executeUpdate(sqlStatement, contextManager);
    }
    
    private void checkBeforeUpdate(final DistSQLUpdateExecutor<?> executor) {
        if (null != executor.getClass().getAnnotation(DistSQLExecutorClusterModeRequired.class)) {
            ShardingSpherePreconditions.checkState(contextManager.getInstanceContext().isCluster(), () -> new UnsupportedSQLOperationException("Mode must be `Cluster`."));
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setRule(final DistSQLExecutorRuleAware executor) throws UnsupportedSQLOperationException {
        Optional<ShardingSphereRule> rule = findRule(executor.getRuleClass());
        ShardingSpherePreconditions.checkState(rule.isPresent(), () -> new UnsupportedSQLOperationException(String.format("The current database has no `%s` rules", executor.getRuleClass())));
        executor.setRule(rule.get());
    }
    
    private Optional<ShardingSphereRule> findRule(final Class<ShardingSphereRule> ruleClass) {
        Optional<ShardingSphereRule> globalRule = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findSingleRule(ruleClass);
        return globalRule.isPresent() ? globalRule : getDatabase(databaseName).getRuleMetaData().findSingleRule(ruleClass);
    }
    
    protected abstract ShardingSphereDatabase getDatabase(String databaseName);
}
