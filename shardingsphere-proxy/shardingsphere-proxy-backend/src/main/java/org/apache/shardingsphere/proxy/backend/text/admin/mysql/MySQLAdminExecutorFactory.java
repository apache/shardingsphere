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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql;

import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutorFactory;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.NoResourceSetExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.NoResourceShowExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowConnectionIdExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowCreateDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowCurrentDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowCurrentUserExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowDatabasesExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowFunctionStatusExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowProcedureStatusExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowProcessListExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowTablesExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowTablesStatusExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowTransactionExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowVersionExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.UnicastResourceShowExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.UseDatabaseExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.UseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowFunctionStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowProcedureStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowProcessListStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;

import java.util.Optional;

/**
 * Admin executor factory for MySQL.
 */
public final class MySQLAdminExecutorFactory implements DatabaseAdminExecutorFactory {
    
    private static final String INFORMATION_SCHEMA = "information_schema";
    
    private static final String PERFORMANCE_SCHEMA = "performance_schema";
    
    @Override
    public Optional<DatabaseAdminExecutor> newInstance(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof MySQLShowFunctionStatusStatement) {
            return Optional.of(new ShowFunctionStatusExecutor((MySQLShowFunctionStatusStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowProcedureStatusStatement) {
            return Optional.of(new ShowProcedureStatusExecutor((MySQLShowProcedureStatusStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowTablesStatement) {
            return Optional.of(new ShowTablesExecutor((MySQLShowTablesStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowTableStatusStatement) {
            return Optional.of(new ShowTablesStatusExecutor((MySQLShowTableStatusStatement) sqlStatement));
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<DatabaseAdminExecutor> newInstance(final SQLStatement sqlStatement, final String sql, final Optional<String> schemaName) {
        if (sqlStatement instanceof UseStatement) {
            return Optional.of(new UseDatabaseExecutor((UseStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowDatabasesStatement) {
            return Optional.of(new ShowDatabasesExecutor((MySQLShowDatabasesStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowProcessListStatement) {
            return Optional.of(new ShowProcessListExecutor());
        }
        if (sqlStatement instanceof MySQLShowCreateDatabaseStatement) {
            return Optional.of(new ShowCreateDatabaseExecutor((MySQLShowCreateDatabaseStatement) sqlStatement));
        }
        if (sqlStatement instanceof SetStatement) {
            if (!hasSchemas() || !hasResources()) {
                return Optional.of(new NoResourceSetExecutor((SetStatement) sqlStatement));
            }
        }
        if (sqlStatement instanceof SelectStatement) {
            if (isShowSpecialFunction((SelectStatement) sqlStatement, ShowConnectionIdExecutor.FUNCTION_NAME)) {
                return Optional.of(new ShowConnectionIdExecutor());
            }
            if (isShowSpecialFunction((SelectStatement) sqlStatement, ShowVersionExecutor.FUNCTION_NAME)) {
                return Optional.of(new ShowVersionExecutor());
            }
            if (isShowSpecialFunction((SelectStatement) sqlStatement, ShowCurrentUserExecutor.FUNCTION_NAME)
                    || isShowSpecialFunction((SelectStatement) sqlStatement, ShowCurrentUserExecutor.FUNCTION_NAME_ALIAS)) {
                return Optional.of(new ShowCurrentUserExecutor());
            }
            if ((!hasSchemas() || !hasResources()) && isShowSpecialFunction((SelectStatement) sqlStatement, ShowTransactionExecutor.TRANSACTION_READ_ONLY)) {
                return Optional.of(new ShowTransactionExecutor(ShowTransactionExecutor.TRANSACTION_READ_ONLY));
            }
            if ((!hasSchemas() || !hasResources()) && isShowSpecialFunction((SelectStatement) sqlStatement, ShowTransactionExecutor.TRANSACTION_ISOLATION)) {
                return Optional.of(new ShowTransactionExecutor(ShowTransactionExecutor.TRANSACTION_ISOLATION));
            }
            if (isShowSpecialFunction((SelectStatement) sqlStatement, ShowCurrentDatabaseExecutor.FUNCTION_NAME)) {
                return Optional.of(new ShowCurrentDatabaseExecutor());
            }
            if (isQueryInformationSchema((SelectStatement) sqlStatement)) {
                return Optional.of(MySQLInformationSchemaExecutorFactory.newInstance((SelectStatement) sqlStatement, sql));
            }
            if (isQueryPerformanceSchema((SelectStatement) sqlStatement)) {
                // TODO
                return Optional.empty();
            }
            return Optional.ofNullable(mockExecutor(schemaName, (SelectStatement) sqlStatement, sql));
        }
        return Optional.empty();
    }
    
    private boolean isShowSpecialFunction(final SelectStatement sqlStatement, final String functionName) {
        ProjectionSegment firstProjection = sqlStatement.getProjections().getProjections().iterator().next();
        return firstProjection instanceof ExpressionProjectionSegment && functionName.equalsIgnoreCase(((ExpressionProjectionSegment) firstProjection).getText());
    }
    
    private boolean isQueryInformationSchema(final SelectStatement sqlStatement) {
        return isQuerySpecialSchema(sqlStatement, INFORMATION_SCHEMA);
    }
    
    private boolean isQueryPerformanceSchema(final SelectStatement sqlStatement) {
        return isQuerySpecialSchema(sqlStatement, PERFORMANCE_SCHEMA);
    }
    
    private boolean isQuerySpecialSchema(final SelectStatement sqlStatement, final String specialSchemaName) {
        TableSegment tableSegment = sqlStatement.getFrom();
        if (!(tableSegment instanceof SimpleTableSegment)) {
            return false;
        }
        return ((SimpleTableSegment) tableSegment).getOwner().isPresent() && specialSchemaName.equalsIgnoreCase(((SimpleTableSegment) tableSegment).getOwner().get().getIdentifier().getValue());
    }
    
    private DatabaseAdminExecutor mockExecutor(final Optional<String> schemaName, final SelectStatement sqlStatement, final String sql) {
        boolean isNotUseSchema = !schemaName.isPresent() && sqlStatement.getFrom() == null;
        if (isNotUseSchema) {
            if (!hasSchemas() || !hasResources()) {
                return new NoResourceShowExecutor(sqlStatement);
            } else {
                return new UnicastResourceShowExecutor(sqlStatement, sql);
            }
        }
        return null;
    }
    
    private boolean hasSchemas() {
        return !ProxyContext.getInstance().getAllSchemaNames().isEmpty();
    }
    
    private boolean hasResources() {
        return ProxyContext.getInstance().getAllSchemaNames().stream().anyMatch(each -> ProxyContext.getInstance().getMetaData(each).hasDataSource());
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
