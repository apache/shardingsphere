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
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutorCreator;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.KillProcessExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.MySQLSetVariableAdminExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.MySQLSystemVariableQueryExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.NoResourceShowExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowConnectionIdExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowCreateDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowCurrentDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowCurrentUserExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowDatabasesExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowFunctionStatusExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowProcedureStatusExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowProcessListExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowTablesExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowVersionExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.UnicastResourceShowExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.UseDatabaseExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLKillStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.function.MySQLShowFunctionStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.procedure.MySQLShowProcedureStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.process.MySQLShowProcessListStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowTablesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLUseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Database admin executor creator for MySQL.
 */
public final class MySQLAdminExecutorCreator implements DatabaseAdminExecutorCreator {
    
    private static final String INFORMATION_SCHEMA = "information_schema";
    
    private static final String MYSQL_SCHEMA = "mysql";
    
    private static final String PERFORMANCE_SCHEMA = "performance_schema";
    
    private static final String SYS_SCHEMA = "sys";
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext sqlStatementContext) {
        return Optional.empty();
    }
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext sqlStatementContext, final String sql, final String databaseName, final List<Object> parameters) {
        if (sqlStatementContext instanceof SelectStatementContext) {
            return createExecutorForSelectStatement((SelectStatementContext) sqlStatementContext, sql, databaseName, parameters);
        }
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof MySQLUseStatement) {
            return Optional.of(new UseDatabaseExecutor((MySQLUseStatement) sqlStatement));
        }
        if (sqlStatement instanceof ShowDatabasesStatement) {
            return Optional.of(new ShowDatabasesExecutor((ShowDatabasesStatement) sqlStatement));
        }
        if (sqlStatement instanceof ShowTablesStatement) {
            return Optional.of(new ShowTablesExecutor((ShowTablesStatement) sqlStatement, sqlStatementContext.getDatabaseType()));
        }
        if (sqlStatement instanceof MySQLShowCreateDatabaseStatement) {
            return Optional.of(new ShowCreateDatabaseExecutor((MySQLShowCreateDatabaseStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowFunctionStatusStatement) {
            return Optional.of(new ShowFunctionStatusExecutor((MySQLShowFunctionStatusStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowProcedureStatusStatement) {
            return Optional.of(new ShowProcedureStatusExecutor((MySQLShowProcedureStatusStatement) sqlStatement));
        }
        if (sqlStatement instanceof SetStatement) {
            return Optional.of(new MySQLSetVariableAdminExecutor((SetStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowProcessListStatement) {
            return Optional.of(new ShowProcessListExecutor(((MySQLShowProcessListStatement) sqlStatement).isFull()));
        }
        if (sqlStatement instanceof MySQLKillStatement) {
            return Optional.of(new KillProcessExecutor((MySQLKillStatement) sqlStatement));
        }
        return Optional.empty();
    }
    
    private Optional<DatabaseAdminExecutor> createExecutorForSelectStatement(final SelectStatementContext selectStatementContext, final String sql,
                                                                             final String databaseName, final List<Object> parameters) {
        if (!selectStatementContext.getSqlStatement().getFrom().isPresent()) {
            return findAdminExecutorForSelectWithoutFrom(selectStatementContext.getDatabaseType(), selectStatementContext.getSqlStatement(), sql, databaseName);
        }
        if (INFORMATION_SCHEMA.equalsIgnoreCase(databaseName) && !ProxyContext.getInstance().getContextManager().getDatabase(databaseName).isComplete()) {
            return MySQLInformationSchemaExecutorFactory.newInstance(selectStatementContext, sql, parameters);
        }
        if (PERFORMANCE_SCHEMA.equalsIgnoreCase(databaseName) && !ProxyContext.getInstance().getContextManager().getDatabase(databaseName).isComplete()) {
            return MySQLPerformanceSchemaExecutorFactory.newInstance(selectStatementContext, sql, parameters);
        }
        if (MYSQL_SCHEMA.equalsIgnoreCase(databaseName) && !ProxyContext.getInstance().getContextManager().getDatabase(databaseName).isComplete()) {
            return MySQLMySQLSchemaExecutorFactory.newInstance(selectStatementContext, sql, parameters);
        }
        if (SYS_SCHEMA.equalsIgnoreCase(databaseName) && !ProxyContext.getInstance().getContextManager().getDatabase(databaseName).isComplete()) {
            return MySQLSysSchemaExecutorFactory.newInstance(selectStatementContext, sql, parameters);
        }
        return Optional.empty();
    }
    
    private Optional<DatabaseAdminExecutor> findAdminExecutorForSelectWithoutFrom(final DatabaseType databaseType,
                                                                                  final SelectStatement selectStatement, final String sql, final String databaseName) {
        Optional<DatabaseAdminExecutor> result = MySQLSystemVariableQueryExecutor.tryGetSystemVariableQueryExecutor(selectStatement);
        return result.isPresent() ? result : getSelectFunctionOrVariableExecutor(databaseType, selectStatement, sql, databaseName);
    }
    
    private Optional<DatabaseAdminExecutor> getSelectFunctionOrVariableExecutor(final DatabaseType databaseType, final SelectStatement selectStatement, final String sql, final String databaseName) {
        if (isShowSpecialFunction(selectStatement, ShowConnectionIdExecutor.FUNCTION_NAME)) {
            return Optional.of(new ShowConnectionIdExecutor(selectStatement));
        }
        if (isShowSpecialFunction(selectStatement, ShowVersionExecutor.FUNCTION_NAME)) {
            return Optional.of(new ShowVersionExecutor(selectStatement));
        }
        if (isShowSpecialFunction(selectStatement, ShowCurrentUserExecutor.FUNCTION_NAME) || isShowSpecialFunction(selectStatement, ShowCurrentUserExecutor.FUNCTION_NAME_ALIAS)) {
            return Optional.of(new ShowCurrentUserExecutor());
        }
        if (isShowSpecialFunction(selectStatement, ShowCurrentDatabaseExecutor.FUNCTION_NAME)) {
            return Optional.of(new ShowCurrentDatabaseExecutor());
        }
        return mockExecutor(databaseName, databaseType, selectStatement, sql);
    }
    
    private boolean isShowSpecialFunction(final SelectStatement sqlStatement, final String functionName) {
        Iterator<ProjectionSegment> segmentIterator = sqlStatement.getProjections().getProjections().iterator();
        ProjectionSegment firstProjection = segmentIterator.next();
        return !segmentIterator.hasNext() && firstProjection instanceof ExpressionProjectionSegment && functionName.equalsIgnoreCase(((ExpressionProjectionSegment) firstProjection).getText());
    }
    
    private Optional<DatabaseAdminExecutor> mockExecutor(final String databaseName, final DatabaseType databaseType, final SelectStatement sqlStatement, final String sql) {
        if (hasNoResource()) {
            return Optional.of(new NoResourceShowExecutor(sqlStatement));
        }
        boolean isUseSchema = null != databaseName || sqlStatement.getFrom().isPresent();
        return isUseSchema ? Optional.empty() : Optional.of(new UnicastResourceShowExecutor(databaseType, sqlStatement, sql));
    }
    
    private boolean hasNoResource() {
        Collection<String> databaseNames = ProxyContext.getInstance().getAllDatabaseNames();
        if (databaseNames.isEmpty()) {
            return true;
        }
        for (String each : databaseNames) {
            if (ProxyContext.getInstance().getContextManager().getDatabase(each).containsDataSource()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
