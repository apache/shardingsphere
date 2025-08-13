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

package org.apache.shardingsphere.proxy.backend.opengauss.handler.admin;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutorCreator;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor.OpenGaussShowVariableExecutor;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.PostgreSQLAdminExecutorCreator;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowStatement;

import java.util.List;
import java.util.Optional;

/**
 * Database admin executor creator for openGauss.
 */
public final class OpenGaussAdminExecutorCreator implements DatabaseAdminExecutorCreator {
    
    private final PostgreSQLAdminExecutorCreator delegated = new PostgreSQLAdminExecutorCreator();
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext sqlStatementContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        return sqlStatement instanceof ShowStatement ? Optional.of(new OpenGaussShowVariableExecutor((ShowStatement) sqlStatement)) : Optional.empty();
    }
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext sqlStatementContext, final String sql, final String databaseName, final List<Object> parameters) {
        OpenGaussSystemTableQueryExecutorCreator systemTableQueryExecutorCreator = new OpenGaussSystemTableQueryExecutorCreator(sqlStatementContext, sql, parameters);
        if (systemTableQueryExecutorCreator.accept()) {
            return systemTableQueryExecutorCreator.create();
        }
        OpenGaussSystemFunctionQueryExecutorCreator functionQueryExecutorCreator = new OpenGaussSystemFunctionQueryExecutorCreator(sqlStatementContext);
        if (functionQueryExecutorCreator.accept()) {
            return functionQueryExecutorCreator.create();
        }
        return delegated.create(sqlStatementContext, sql, databaseName, parameters);
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
