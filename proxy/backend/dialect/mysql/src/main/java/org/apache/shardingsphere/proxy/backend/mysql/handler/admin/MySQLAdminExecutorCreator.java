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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutorCreator;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.MySQLKillProcessExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.MySQLSetVariableAdminExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowCreateDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowDatabasesExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowFunctionStatusExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowProcedureStatusExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowProcessListExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowTablesExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.MySQLUseDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.factory.MySQLSelectAdminExecutorFactory;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLKillStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLUseStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.function.MySQLShowFunctionStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.procedure.MySQLShowProcedureStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.process.MySQLShowProcessListStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowTablesStatement;

import java.util.List;
import java.util.Optional;

/**
 * Database admin executor creator for MySQL.
 */
public final class MySQLAdminExecutorCreator implements DatabaseAdminExecutorCreator {
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext sqlStatementContext, final String sql, final String databaseName, final List<Object> parameters) {
        if (sqlStatementContext instanceof SelectStatementContext) {
            return MySQLSelectAdminExecutorFactory.newInstance((SelectStatementContext) sqlStatementContext, sql, databaseName, parameters);
        }
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof MySQLUseStatement) {
            return Optional.of(new MySQLUseDatabaseExecutor((MySQLUseStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowDatabasesStatement) {
            return Optional.of(new MySQLShowDatabasesExecutor((MySQLShowDatabasesStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowTablesStatement) {
            return Optional.of(new MySQLShowTablesExecutor((MySQLShowTablesStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowCreateDatabaseStatement) {
            return Optional.of(new MySQLShowCreateDatabaseExecutor((MySQLShowCreateDatabaseStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowFunctionStatusStatement) {
            return Optional.of(new MySQLShowFunctionStatusExecutor((MySQLShowFunctionStatusStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowProcedureStatusStatement) {
            return Optional.of(new MySQLShowProcedureStatusExecutor((MySQLShowProcedureStatusStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowProcessListStatement) {
            return Optional.of(new MySQLShowProcessListExecutor(((MySQLShowProcessListStatement) sqlStatement).isFull()));
        }
        if (sqlStatement instanceof MySQLKillStatement) {
            return Optional.of(new MySQLKillProcessExecutor((MySQLKillStatement) sqlStatement));
        }
        if (sqlStatement instanceof SetStatement) {
            return Optional.of(new MySQLSetVariableAdminExecutor((SetStatement) sqlStatement));
        }
        return Optional.empty();
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
