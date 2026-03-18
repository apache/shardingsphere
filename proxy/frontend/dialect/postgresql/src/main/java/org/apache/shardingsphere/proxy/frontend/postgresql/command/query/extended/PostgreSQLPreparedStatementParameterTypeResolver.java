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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Parameter type resolver for PostgreSQL prepared statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLPreparedStatementParameterTypeResolver {
    
    /**
     * Resolve unspecified parameter types by JDBC metadata.
     *
     * @param connectionSession connection session
     * @param preparedStatement prepared statement
     * @param parameters parameters
     * @throws SQLException SQL exception
     */
    public static void resolveParameterTypes(final ConnectionSession connectionSession, final PostgreSQLServerPreparedStatement preparedStatement, final List<Object> parameters) throws SQLException {
        if (!hasUnspecifiedParameterTypes(preparedStatement)) {
            return;
        }
        try (PreparedStatement actualPreparedStatement = PostgreSQLPreparedStatementMetadataFactory.load(connectionSession, preparedStatement, parameters)) {
            resolveParameterTypes(preparedStatement, actualPreparedStatement);
        }
    }
    
    /**
     * Resolve unspecified parameter types by prepared statement metadata.
     *
     * @param preparedStatement prepared statement
     * @param actualPreparedStatement actual prepared statement
     * @throws SQLException SQL exception
     */
    public static void resolveParameterTypes(final PostgreSQLServerPreparedStatement preparedStatement, final PreparedStatement actualPreparedStatement) throws SQLException {
        if (!hasUnspecifiedParameterTypes(preparedStatement)) {
            return;
        }
        ParameterMetaData parameterMetaData = actualPreparedStatement.getParameterMetaData();
        for (int i = 0; i < preparedStatement.getSqlStatementContext().getSqlStatement().getParameterCount(); i++) {
            if (PostgreSQLBinaryColumnType.UNSPECIFIED == preparedStatement.getParameterTypes().get(i)) {
                preparedStatement.getParameterTypes().set(i, PostgreSQLBinaryColumnType.valueOfJDBCType(parameterMetaData.getParameterType(i + 1), parameterMetaData.getParameterTypeName(i + 1)));
            }
        }
    }
    
    private static boolean hasUnspecifiedParameterTypes(final PostgreSQLServerPreparedStatement preparedStatement) {
        return 0 != preparedStatement.getSqlStatementContext().getSqlStatement().getParameterCount()
                && preparedStatement.getParameterTypes().stream().anyMatch(each -> PostgreSQLBinaryColumnType.UNSPECIFIED == each);
    }
}
