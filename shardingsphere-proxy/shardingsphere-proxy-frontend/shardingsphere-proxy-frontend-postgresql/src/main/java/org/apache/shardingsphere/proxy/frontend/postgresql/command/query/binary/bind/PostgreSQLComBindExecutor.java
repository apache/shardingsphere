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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.bind;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.binary.BinaryCell;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLColumnFormat;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.PostgreSQLBindCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.text.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.data.impl.BinaryQueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandlerFactory;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Command bind executor for PostgreSQL.
 */
@RequiredArgsConstructor
public final class PostgreSQLComBindExecutor implements QueryCommandExecutor {
    
    private final PostgreSQLConnectionContext connectionContext;
    
    private final PostgreSQLComBindPacket packet;
    
    private final BackendConnection backendConnection;
    
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    private TextProtocolBackendHandler textProtocolBackendHandler;
    
    @Getter
    private volatile ResponseType responseType;
    
    @Override
    public Collection<DatabasePacket<?>> execute() throws SQLException {
        init();
        List<DatabasePacket<?>> result = new LinkedList<>();
        result.add(new PostgreSQLBindCompletePacket());
        if (null == databaseCommunicationEngine && null == textProtocolBackendHandler) {
            return result;
        }
        ResponseHeader responseHeader = null != databaseCommunicationEngine ? databaseCommunicationEngine.execute() : textProtocolBackendHandler.execute();
        if (responseHeader instanceof QueryResponseHeader && connectionContext.getDescribeExecutor().isPresent()) {
            connectionContext.getDescribeExecutor().get().setRowDescriptionPacket(getRowDescriptionPacket((QueryResponseHeader) responseHeader));
        }
        if (responseHeader instanceof UpdateResponseHeader) {
            responseType = ResponseType.UPDATE;
            connectionContext.setUpdateCount(((UpdateResponseHeader) responseHeader).getUpdateCount());
        }
        return result;
    }
    
    private void init() throws SQLException {
        SQLStatement sqlStatement = getSqlStatement();
        if (sqlStatement instanceof TCLStatement || sqlStatement instanceof EmptyStatement) {
            textProtocolBackendHandler = TextProtocolBackendHandlerFactory.newInstance(DatabaseTypeRegistry.getActualDatabaseType("PostgreSQL"), packet.getSql(), backendConnection);
            return;
        }
        databaseCommunicationEngine = DatabaseCommunicationEngineFactory.getInstance().newBinaryProtocolInstance(sqlStatement, packet.getSql(), packet.getParameters(), backendConnection);
    }
    
    private SQLStatement getSqlStatement() {
        return connectionContext.getSqlStatement().orElseGet(() -> {
            SQLStatement result = parseSql(packet.getSql(), backendConnection.getSchemaName());
            connectionContext.setSqlStatement(result);
            return result;
        });
    }
    
    private SQLStatement parseSql(final String sql, final String schemaName) {
        if (sql.isEmpty()) {
            return new EmptyStatement();
        }
        ShardingSphereSQLParserEngine sqlStatementParserEngine = new ShardingSphereSQLParserEngine(
                DatabaseTypeRegistry.getTrunkDatabaseTypeName(ProxyContext.getInstance().getMetaDataContexts().getMetaData(schemaName).getResource().getDatabaseType()));
        return sqlStatementParserEngine.parse(sql, true);
    }
    
    private PostgreSQLRowDescriptionPacket getRowDescriptionPacket(final QueryResponseHeader queryResponseHeader) {
        responseType = ResponseType.QUERY;
        Collection<PostgreSQLColumnDescription> columnDescriptions = createColumnDescriptions(queryResponseHeader);
        return new PostgreSQLRowDescriptionPacket(columnDescriptions.size(), columnDescriptions);
    }
    
    private Collection<PostgreSQLColumnDescription> createColumnDescriptions(final QueryResponseHeader queryResponseHeader) {
        Collection<PostgreSQLColumnDescription> result = new LinkedList<>();
        int columnIndex = 0;
        for (QueryHeader each : queryResponseHeader.getQueryHeaders()) {
            result.add(new PostgreSQLColumnDescription(each.getColumnName(), ++columnIndex, each.getColumnType(), each.getColumnLength(), each.getColumnTypeName()));
        }
        return result;
    }
    
    @Override
    public boolean next() throws SQLException {
        return null != databaseCommunicationEngine && databaseCommunicationEngine.next();
    }
    
    @Override
    public PostgreSQLPacket getQueryRowPacket() throws SQLException {
        QueryResponseRow queryResponseRow = databaseCommunicationEngine.getQueryResponseRow();
        return new PostgreSQLDataRowPacket(getData(queryResponseRow));
    }
    
    private List<Object> getData(final QueryResponseRow queryResponseRow) {
        Collection<QueryResponseCell> cells = queryResponseRow.getCells();
        List<Object> result = new ArrayList<>(cells.size());
        List<QueryResponseCell> columns = new ArrayList<>(cells);
        for (int i = 0; i < columns.size(); i++) {
            PostgreSQLColumnFormat format = packet.getResultFormatByColumnIndex(i);
            result.add(PostgreSQLColumnFormat.BINARY == format ? createBinaryCell(columns.get(i)) : columns.get(i).getData());
        }
        return result;
    }
    
    private BinaryCell createBinaryCell(final QueryResponseCell cell) {
        return new BinaryCell(PostgreSQLBinaryColumnType.valueOfJDBCType(((BinaryQueryResponseCell) cell).getJdbcType()), cell.getData());
    }
}
