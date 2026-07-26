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

package org.apache.shardingsphere.proxy.backend.postgresql.connector.jdbc;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.DialectJDBCResultMetadataChecker;
import org.apache.shardingsphere.proxy.backend.postgresql.exception.PostgreSQLCompositeTypeAcrossDataSourcesException;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;

/**
 * JDBC result metadata checker for PostgreSQL.
 */
public final class PostgreSQLJDBCResultMetadataChecker implements DialectJDBCResultMetadataChecker {
    
    @Override
    public void check(final ExecutionContext executionContext, final Statement statement, final String sql) throws SQLException {
        if (!isRoutedToMultipleDataSources(executionContext.getExecutionUnits())) {
            return;
        }
        if (statement instanceof PreparedStatement) {
            checkCompositeType(((PreparedStatement) statement).getMetaData(), executionContext.getSqlStatementContext());
            return;
        }
        try (PreparedStatement preparedStatement = statement.getConnection().prepareStatement(sql)) {
            checkCompositeType(preparedStatement.getMetaData(), executionContext.getSqlStatementContext());
        }
    }
    
    private boolean isRoutedToMultipleDataSources(final Collection<ExecutionUnit> executionUnits) {
        String dataSourceName = executionUnits.iterator().next().getDataSourceName();
        for (ExecutionUnit each : executionUnits) {
            if (!dataSourceName.equals(each.getDataSourceName())) {
                return true;
            }
        }
        return false;
    }
    
    private void checkCompositeType(final ResultSetMetaData metaData, final SQLStatementContext sqlStatementContext) throws SQLException {
        if (null == metaData) {
            return;
        }
        int columnCount = sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).containsDerivedProjections()
                ? ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections().size()
                : metaData.getColumnCount();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            ShardingSpherePreconditions.checkState(Types.STRUCT != metaData.getColumnType(columnIndex), PostgreSQLCompositeTypeAcrossDataSourcesException::new);
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
