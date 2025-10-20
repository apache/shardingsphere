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

package org.apache.shardingsphere.proxy.backend.postgresql.handler.admin;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutorCreator;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.executor.PostgreSQLResetVariableAdminExecutor;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.executor.PostgreSQLSetVariableAdminExecutor;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.executor.PostgreSQLShowVariableExecutor;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.factory.PostgreSQLSelectAdminExecutorFactory;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dal.PostgreSQLResetParameterStatement;

import java.util.List;
import java.util.Optional;

/**
 * Database admin executor creator for PostgreSQL.
 */
public final class PostgreSQLAdminExecutorCreator implements DatabaseAdminExecutorCreator {
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext sqlStatementContext, final String sql, final String databaseName, final List<Object> parameters) {
        if (sqlStatementContext instanceof SelectStatementContext) {
            return PostgreSQLSelectAdminExecutorFactory.newInstance((SelectStatementContext) sqlStatementContext, sql, parameters);
        }
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof SetStatement) {
            return Optional.of(new PostgreSQLSetVariableAdminExecutor((SetStatement) sqlStatement));
        }
        if (sqlStatement instanceof ShowStatement) {
            return Optional.of(new PostgreSQLShowVariableExecutor((ShowStatement) sqlStatement));
        }
        if (sqlStatement instanceof PostgreSQLResetParameterStatement) {
            return Optional.of(new PostgreSQLResetVariableAdminExecutor((PostgreSQLResetParameterStatement) sqlStatement));
        }
        return Optional.empty();
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
