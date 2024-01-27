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

package org.apache.shardingsphere.distsql.handler.type.rql;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.type.rql.aware.DatabaseRuleAwareRQLExecutor;
import org.apache.shardingsphere.distsql.handler.type.rql.aware.GlobalRuleAwareRQLExecutor;
import org.apache.shardingsphere.distsql.handler.util.DatabaseNameUtils;
import org.apache.shardingsphere.distsql.statement.rql.RQLStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * RQL execute engine.
 */
@RequiredArgsConstructor
public abstract class RQLExecuteEngine {
    
    private final RQLStatement sqlStatement;
    
    private final String currentDatabaseName;
    
    private final ContextManager contextManager;
    
    @Getter
    private Collection<String> columnNames;
    
    @Getter
    private Collection<LocalDataQueryResultRow> rows;
    
    /**
     * Execute query.
     * 
     * @throws SQLException SQL exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void executeQuery() throws SQLException {
        RQLExecutor executor = TypedSPILoader.getService(RQLExecutor.class, sqlStatement.getClass());
        columnNames = executor.getColumnNames();
        if (executor instanceof DistSQLExecutorDatabaseAware) {
            setUpDatabaseAwareExecutor((DistSQLExecutorDatabaseAware) executor);
        }
        if (executor instanceof GlobalRuleAwareRQLExecutor) {
            setUpGlobalRuleAwareExecutor((GlobalRuleAwareRQLExecutor) executor);
        }
        if (null == rows) {
            rows = executor.getRows(sqlStatement, contextManager);
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setUpDatabaseAwareExecutor(final DistSQLExecutorDatabaseAware executor) {
        ShardingSphereDatabase database = getDatabase(DatabaseNameUtils.getDatabaseName(sqlStatement, currentDatabaseName));
        executor.setDatabase(database);
        if (executor instanceof DatabaseRuleAwareRQLExecutor) {
            Optional<ShardingSphereRule> rule = database.getRuleMetaData().findSingleRule(((DatabaseRuleAwareRQLExecutor) executor).getRuleClass());
            if (rule.isPresent()) {
                ((DatabaseRuleAwareRQLExecutor) executor).setRule(rule.get());
            } else {
                rows = Collections.emptyList();
            }
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setUpGlobalRuleAwareExecutor(final GlobalRuleAwareRQLExecutor executor) {
        Optional<ShardingSphereRule> rule = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findSingleRule(executor.getRuleClass());
        if (rule.isPresent()) {
            executor.setRule(rule.get());
        } else {
            rows = Collections.emptyList();
        }
    }
    
    protected abstract ShardingSphereDatabase getDatabase(String databaseName);
}
