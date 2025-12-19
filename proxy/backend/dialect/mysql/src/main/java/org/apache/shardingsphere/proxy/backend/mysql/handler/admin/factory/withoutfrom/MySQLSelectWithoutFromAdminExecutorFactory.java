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
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.MySQLSystemVariableQueryExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.NoResourceShowExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.ShowConnectionIdExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.ShowCurrentDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.ShowCurrentUserExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.ShowVersionExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.UnicastResourceShowExecutor;
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
     * @param metaData meta data
     * @return created instance
     */
    public static Optional<DatabaseAdminExecutor> newInstance(final SelectStatementContext selectStatementContext, final String sql,
                                                              final String databaseName, final ShardingSphereMetaData metaData) {
        SelectStatement selectStatement = selectStatementContext.getSqlStatement();
        Optional<DatabaseAdminExecutor> result = MySQLSystemVariableQueryExecutor.tryGetSystemVariableQueryExecutor(selectStatement);
        return result.isPresent() ? result : getSelectFunctionOrVariableExecutor(selectStatement, sql, databaseName, metaData);
    }
    
    private static Optional<DatabaseAdminExecutor> getSelectFunctionOrVariableExecutor(final SelectStatement selectStatement, final String sql,
                                                                                       final String databaseName, final ShardingSphereMetaData metaData) {
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
        return mockExecutor(selectStatement, sql, databaseName, metaData);
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
    
    private static Optional<DatabaseAdminExecutor> mockExecutor(final SelectStatement sqlStatement, final String sql, final String databaseName, final ShardingSphereMetaData metaData) {
        if (isEmptyResource(metaData)) {
            return Optional.of(new NoResourceShowExecutor(sqlStatement));
        }
        boolean isUseDatabase = null != databaseName || sqlStatement.getFrom().isPresent();
        if (!isUseDatabase && hasMultipleProjections(sqlStatement)) {
            return Optional.empty();
        }
        return isUseDatabase ? Optional.empty() : Optional.of(new UnicastResourceShowExecutor(sqlStatement, sql));
    }
    
    private static boolean hasMultipleProjections(final SelectStatement sqlStatement) {
        Collection<ProjectionSegment> projections = sqlStatement.getProjections().getProjections();
        return projections.size() > 1;
    }
    
    private static boolean isEmptyResource(final ShardingSphereMetaData metaData) {
        Collection<ShardingSphereDatabase> databases = metaData.getAllDatabases();
        return databases.isEmpty() || databases.stream().noneMatch(ShardingSphereDatabase::containsDataSource);
    }
}
