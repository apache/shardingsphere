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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.factory.withoutfrom;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.MySQLSystemVariableQueryExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.NoResourceShowExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowConnectionIdExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowCurrentDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowCurrentUserExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowVersionExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.UnicastResourceShowExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

/**
 * Select without from admin executor factory for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLSelectWithoutFromAdminExecutorFactory {
    
    /**
     * New instance of select without from admin executor factory for MySQL.
     *
     * @param selectStatementContext select statement context
     * @param sql SQL
     * @param databaseName database name
     * @return created instance
     */
    public static Optional<DatabaseAdminExecutor> newInstance(final SelectStatementContext selectStatementContext, final String sql, final String databaseName) {
        SelectStatement selectStatement = selectStatementContext.getSqlStatement();
        Optional<DatabaseAdminExecutor> result = MySQLSystemVariableQueryExecutor.tryGetSystemVariableQueryExecutor(selectStatement);
        return result.isPresent() ? result : getSelectFunctionOrVariableExecutor(selectStatement, sql, databaseName);
    }
    
    private static Optional<DatabaseAdminExecutor> getSelectFunctionOrVariableExecutor(final SelectStatement selectStatement, final String sql, final String databaseName) {
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
        return mockExecutor(databaseName, selectStatement, sql);
    }
    
    private static boolean isShowSpecialFunction(final SelectStatement sqlStatement, final String functionName) {
        Iterator<ProjectionSegment> segmentIterator = sqlStatement.getProjections().getProjections().iterator();
        ProjectionSegment firstProjection = segmentIterator.next();
        if (segmentIterator.hasNext() || !(firstProjection instanceof ExpressionProjectionSegment)) {
            return false;
        }
        String projectionText = ((ExpressionProjectionSegment) firstProjection).getText();
        String trimmedText = projectionText.replaceAll("\\s+\\(", "(");
        return functionName.equalsIgnoreCase(trimmedText);
    }
    
    private static Optional<DatabaseAdminExecutor> mockExecutor(final String databaseName, final SelectStatement sqlStatement, final String sql) {
        if (isEmptyResource()) {
            return Optional.of(new NoResourceShowExecutor(sqlStatement));
        }
        boolean isUseDatabase = null != databaseName || sqlStatement.getFrom().isPresent();
        return isUseDatabase ? Optional.empty() : Optional.of(new UnicastResourceShowExecutor(sqlStatement, sql));
    }
    
    private static boolean isEmptyResource() {
        Collection<ShardingSphereDatabase> databases = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getAllDatabases();
        return databases.isEmpty() || databases.stream().noneMatch(ShardingSphereDatabase::containsDataSource);
    }
}
