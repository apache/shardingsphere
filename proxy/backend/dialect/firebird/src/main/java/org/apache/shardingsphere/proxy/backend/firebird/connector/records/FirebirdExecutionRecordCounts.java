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

package org.apache.shardingsphere.proxy.backend.firebird.connector.records;

import org.apache.shardingsphere.proxy.backend.connector.records.DialectExecutionRecordCounts;
import org.apache.shardingsphere.proxy.backend.connector.records.ExecutionRecordCounts;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.SqlCountHolder;
import org.firebirdsql.jdbc.FBStatement;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Execution record counts for Firebird.
 *
 * <p>Jaybird reports separate insert and update counts through {@code FirebirdStatement} only for statements with an update count result.
 * A cursor statement returns {@code -1} there, so the server record counts are read from the Jaybird statement handle with reflection.</p>
 */
public final class FirebirdExecutionRecordCounts implements DialectExecutionRecordCounts {
    
    private static final Field STATEMENT_HANDLE_FIELD = getStatementHandleField();
    
    @Override
    public ExecutionRecordCounts capture(final Statement statement) throws SQLException {
        SqlCountHolder counts = getSqlCounts(statement.unwrap(FBStatement.class));
        return new ExecutionRecordCounts(counts.getIntegerInsertCount(), counts.getIntegerUpdateCount(), counts.getIntegerSelectCount());
    }
    
    private SqlCountHolder getSqlCounts(final FBStatement statement) throws SQLException {
        try {
            return ((FbStatement) STATEMENT_HANDLE_FIELD.get(statement)).getSqlCounts();
        } catch (final IllegalAccessException ex) {
            throw new SQLException("Failed to access Firebird statement counts.", ex);
        }
    }
    
    private static Field getStatementHandleField() {
        try {
            Field result = FBStatement.class.getDeclaredField("fbStatement");
            result.setAccessible(true);
            return result;
        } catch (final NoSuchFieldException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "Firebird";
    }
}
