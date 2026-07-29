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

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.DialectJDBCResultMetadataChecker;

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
    public void check(final Collection<ExecutionUnit> executionUnits, final Statement statement, final String sql) throws SQLException {
        if (executionUnits.size() <= 1 || isRoutedToSingleStorageUnit(executionUnits)) {
            return;
        }
        if (statement instanceof PreparedStatement) {
            checkCompositeType(((PreparedStatement) statement).getMetaData());
            return;
        }
        try (PreparedStatement preparedStatement = statement.getConnection().prepareStatement(sql)) {
            checkCompositeType(preparedStatement.getMetaData());
        }
    }
    
    private boolean isRoutedToSingleStorageUnit(final Collection<ExecutionUnit> executionUnits) {
        String storageUnitName = executionUnits.iterator().next().getDataSourceName();
        for (ExecutionUnit each : executionUnits) {
            if (!storageUnitName.equals(each.getDataSourceName())) {
                return false;
            }
        }
        return true;
    }
    
    private void checkCompositeType(final ResultSetMetaData metaData) throws SQLException {
        if (null == metaData) {
            return;
        }
        int columnCount = metaData.getColumnCount();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            ShardingSpherePreconditions.checkState(Types.STRUCT != metaData.getColumnType(columnIndex),
                    () -> new UnsupportedSQLOperationException("Composite result columns cannot be returned because the query is routed to multiple storage units"));
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
