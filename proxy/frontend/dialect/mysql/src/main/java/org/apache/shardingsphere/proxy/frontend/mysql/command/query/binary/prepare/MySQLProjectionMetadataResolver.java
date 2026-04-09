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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCharacterSets;
import org.apache.shardingsphere.database.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinitionFlag;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.metadata.JDBCQueryResultMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilderEngine;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * Projection metadata resolver for MySQL prepared statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLProjectionMetadataResolver {
    
    private static final String BINARY_COLUMN_TYPE_KEYWORD = "BINARY";
    
    private static final String BLOB_COLUMN_TYPE_KEYWORD = "BLOB";
    
    private static final Collection<Integer> BINARY_TYPES = new HashSet<>(Arrays.asList(Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY));
    
    /**
     * Resolve projection metadata packets by JDBC metadata.
     *
     * @param connectionSession connection session
     * @param preparedStatement prepared statement
     * @param selectStatementContext select statement context
     * @param characterSet character set
     * @return column definition packets
     * @throws SQLException SQL exception
     */
    public static Collection<MySQLPacket> resolveProjectionPackets(final ConnectionSession connectionSession, final MySQLServerPreparedStatement preparedStatement,
                                                                   final SelectStatementContext selectStatementContext, final int characterSet) throws SQLException {
        try (PreparedStatement actualPreparedStatement = MySQLPreparedStatementMetadataFactory.load(connectionSession, preparedStatement)) {
            ResultSetMetaData resultSetMetaData = actualPreparedStatement.getMetaData();
            if (null == resultSetMetaData) {
                return createDefaultPackets(selectStatementContext, characterSet);
            }
            QueryResultMetaData queryResultMetaData = new JDBCQueryResultMetaData(resultSetMetaData);
            String databaseName = selectStatementContext.getTablesContext().getDatabaseName().orElseGet(connectionSession::getCurrentDatabaseName);
            ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName);
            QueryHeaderBuilderEngine queryHeaderBuilderEngine = new QueryHeaderBuilderEngine(database.getProtocolType());
            Collection<MySQLPacket> result = new ArrayList<>(selectStatementContext.getProjectionsContext().getExpandProjections().size());
            for (int columnIndex = 1; columnIndex <= selectStatementContext.getProjectionsContext().getExpandProjections().size(); columnIndex++) {
                QueryHeader queryHeader = queryHeaderBuilderEngine.build(selectStatementContext.getProjectionsContext(), queryResultMetaData, database, columnIndex);
                result.add(createMySQLColumnDefinition41Packet(queryHeader, characterSet));
            }
            return result;
        }
    }
    
    private static Collection<MySQLPacket> createDefaultPackets(final SelectStatementContext selectStatementContext, final int characterSet) {
        Collection<MySQLPacket> result = new ArrayList<>(selectStatementContext.getProjectionsContext().getExpandProjections().size());
        for (int index = 0; index < selectStatementContext.getProjectionsContext().getExpandProjections().size(); index++) {
            result.add(new MySQLColumnDefinition41Packet(characterSet, 0, "", "", "", "", "", 0, MySQLBinaryColumnType.VAR_STRING, 0, false));
        }
        return result;
    }
    
    private static MySQLColumnDefinition41Packet createMySQLColumnDefinition41Packet(final QueryHeader queryHeader, final int characterSet) {
        int actualCharacterSet = BINARY_TYPES.contains(queryHeader.getColumnType()) ? MySQLCharacterSets.BINARY.getId() : characterSet;
        return new MySQLColumnDefinition41Packet(actualCharacterSet, getColumnDefinitionFlag(queryHeader), queryHeader.getSchema(), queryHeader.getTable(), queryHeader.getTable(),
                queryHeader.getColumnLabel(), queryHeader.getColumnName(), queryHeader.getColumnLength(), MySQLBinaryColumnType.valueOfJDBCType(queryHeader.getColumnType()),
                queryHeader.getDecimals(), false);
    }
    
    private static int getColumnDefinitionFlag(final QueryHeader queryHeader) {
        int result = 0;
        if (queryHeader.isPrimaryKey()) {
            result += MySQLColumnDefinitionFlag.PRIMARY_KEY.getValue();
        }
        if (queryHeader.isNotNull()) {
            result += MySQLColumnDefinitionFlag.NOT_NULL.getValue();
        }
        if (!queryHeader.isSigned()) {
            result += MySQLColumnDefinitionFlag.UNSIGNED.getValue();
        }
        if (queryHeader.isAutoIncrement()) {
            result += MySQLColumnDefinitionFlag.AUTO_INCREMENT.getValue();
        }
        if (queryHeader.getColumnTypeName().contains(BINARY_COLUMN_TYPE_KEYWORD) || queryHeader.getColumnTypeName().contains(BLOB_COLUMN_TYPE_KEYWORD)) {
            result += MySQLColumnDefinitionFlag.BINARY_COLLATION.getValue();
        }
        if (BINARY_TYPES.contains(queryHeader.getColumnType())) {
            result += MySQLColumnDefinitionFlag.BLOB.getValue();
        }
        return result;
    }
}
