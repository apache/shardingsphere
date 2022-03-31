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

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.binary.BinaryCell;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLErrorCode;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLMessageSeverityLevel;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLValueFormat;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLEmptyQueryResponsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLNoDataPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLPreparedStatement;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLPortalSuspendedPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLParameterStatusPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.distsql.parser.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.schema.builder.SystemSchemaBuilderRule;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.JDBCDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.data.impl.BinaryQueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.ClientEncodingResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandlerFactory;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.PostgreSQLCommand;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL portal using JDBC backend.
 */
public final class JDBCPortal implements Portal<Void> {
    
    @Getter
    private final String name;
    
    @Getter
    private final SQLStatement sqlStatement;
    
    private final List<PostgreSQLValueFormat> resultFormats;
    
    private final JDBCDatabaseCommunicationEngine databaseCommunicationEngine;
    
    private final TextProtocolBackendHandler textProtocolBackendHandler;
    
    private final JDBCBackendConnection backendConnection;
    
    private ResponseHeader responseHeader;
    
    public JDBCPortal(final String name, final PostgreSQLPreparedStatement preparedStatement, final List<Object> parameters, final List<PostgreSQLValueFormat> resultFormats,
                      final JDBCBackendConnection backendConnection) throws SQLException {
        this.name = name;
        this.sqlStatement = preparedStatement.getSqlStatement();
        this.resultFormats = resultFormats;
        this.backendConnection = backendConnection;
        if (sqlStatement instanceof TCLStatement || sqlStatement instanceof EmptyStatement || sqlStatement instanceof DistSQLStatement || sqlStatement instanceof SetStatement) {
            databaseCommunicationEngine = null;
            textProtocolBackendHandler = TextProtocolBackendHandlerFactory.newInstance(DatabaseTypeRegistry.getActualDatabaseType("PostgreSQL"),
                    preparedStatement.getSql(), () -> Optional.of(sqlStatement), backendConnection.getConnectionSession());
            return;
        }
        String schemaName = backendConnection.getConnectionSession().getDefaultSchemaName();
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(
                ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataMap(), parameters, sqlStatement, schemaName);
        if (containsSystemTable(sqlStatementContext.getTablesContext().getTableNames())) {
            databaseCommunicationEngine = null;
            DatabaseType databaseType = ProxyContext.getInstance().getMetaData(schemaName).getResource().getDatabaseType();
            textProtocolBackendHandler = TextProtocolBackendHandlerFactory.newInstance(databaseType,
                    preparedStatement.getSql(), () -> Optional.of(sqlStatement), backendConnection.getConnectionSession());
            return;
        }
        databaseCommunicationEngine = DatabaseCommunicationEngineFactory.getInstance().newBinaryProtocolInstance(sqlStatementContext, preparedStatement.getSql(), parameters, backendConnection);
        textProtocolBackendHandler = null;
    }
    
    private boolean containsSystemTable(final Collection<String> tableNames) {
        for (String each : tableNames) {
            if (SystemSchemaBuilderRule.POSTGRESQL_PG_CATALOG.getTables().contains(each)) {
                return true;
            }
        }
        return false;
    }
    
    @SneakyThrows(SQLException.class)
    @Override
    public Void bind() {
        responseHeader = null != databaseCommunicationEngine ? databaseCommunicationEngine.execute() : textProtocolBackendHandler.execute();
        return null;
    }
    
    @Override
    public PostgreSQLPacket describe() {
        if (responseHeader instanceof QueryResponseHeader) {
            return createRowDescriptionPacket((QueryResponseHeader) responseHeader);
        }
        if (responseHeader instanceof UpdateResponseHeader) {
            return PostgreSQLNoDataPacket.getInstance();
        }
        if (responseHeader instanceof ClientEncodingResponseHeader) {
            return PostgreSQLNoDataPacket.getInstance();
        }
        throw new IllegalStateException("Cannot describe portal [" + name + "] before bind");
    }
    
    private PostgreSQLRowDescriptionPacket createRowDescriptionPacket(final QueryResponseHeader queryResponseHeader) {
        Collection<PostgreSQLColumnDescription> columnDescriptions = createColumnDescriptions(queryResponseHeader);
        return new PostgreSQLRowDescriptionPacket(columnDescriptions.size(), columnDescriptions);
    }
    
    private Collection<PostgreSQLColumnDescription> createColumnDescriptions(final QueryResponseHeader queryResponseHeader) {
        Collection<PostgreSQLColumnDescription> result = new LinkedList<>();
        int columnIndex = 0;
        for (QueryHeader each : queryResponseHeader.getQueryHeaders()) {
            result.add(new PostgreSQLColumnDescription(each.getColumnLabel(), ++columnIndex, each.getColumnType(), each.getColumnLength(), each.getColumnTypeName()));
        }
        return result;
    }
    
    @SneakyThrows(SQLException.class)
    @Override
    public List<PostgreSQLPacket> execute(final int maxRows) {
        int fetchSize = maxRows > 0 ? maxRows : Integer.MAX_VALUE;
        List<PostgreSQLPacket> result = new LinkedList<>();
        for (int i = 0; i < fetchSize && hasNext(); i++) {
            result.add(nextPacket());
        }
        if (responseHeader instanceof ClientEncodingResponseHeader) {
            result.addAll(handleSetClientEncoding((ClientEncodingResponseHeader) responseHeader));
            return result;
        }
        result.add(createExecutionCompletedPacket(maxRows > 0 && maxRows == result.size(), result.size()));
        return result;
    }
    
    private Collection<PostgreSQLPacket> handleSetClientEncoding(final ClientEncodingResponseHeader clientEncodingResponseHeader) {
        Collection<PostgreSQLPacket> result = new LinkedList<>();
        Optional<String> currentCharsetValue = clientEncodingResponseHeader.getCurrentCharsetValue();
        if (currentCharsetValue.isPresent()) {
            result.add(new PostgreSQLCommandCompletePacket("SET", 0));
            result.add(new PostgreSQLParameterStatusPacket("client_encoding", currentCharsetValue.get()));
            return result;
        }
        result.add(PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.ERROR, PostgreSQLErrorCode.INVALID_PARAMETER_VALUE,
                String.format("invalid value for parameter \"clientEncoding\": \"%s\"", clientEncodingResponseHeader.getInputValue())).build());
        return result;
    }
    
    private boolean hasNext() throws SQLException {
        return null != databaseCommunicationEngine && databaseCommunicationEngine.next() || null != textProtocolBackendHandler && textProtocolBackendHandler.next();
    }
    
    private PostgreSQLPacket nextPacket() throws SQLException {
        return null != databaseCommunicationEngine ? new PostgreSQLDataRowPacket(getData(databaseCommunicationEngine.getQueryResponseRow()))
                : new PostgreSQLDataRowPacket(textProtocolBackendHandler.getRowData());
    }
    
    private List<Object> getData(final QueryResponseRow queryResponseRow) {
        Collection<QueryResponseCell> cells = queryResponseRow.getCells();
        List<Object> result = new ArrayList<>(cells.size());
        List<QueryResponseCell> columns = new ArrayList<>(cells);
        for (int i = 0; i < columns.size(); i++) {
            PostgreSQLValueFormat format = determineValueFormat(i);
            result.add(PostgreSQLValueFormat.BINARY == format ? createBinaryCell(columns.get(i)) : columns.get(i).getData());
        }
        return result;
    }
    
    private PostgreSQLValueFormat determineValueFormat(final int columnIndex) {
        return resultFormats.isEmpty() ? PostgreSQLValueFormat.TEXT : resultFormats.get(columnIndex % resultFormats.size());
    }
    
    private BinaryCell createBinaryCell(final QueryResponseCell cell) {
        return new BinaryCell(PostgreSQLColumnType.valueOfJDBCType(((BinaryQueryResponseCell) cell).getJdbcType()), cell.getData());
    }
    
    private PostgreSQLIdentifierPacket createExecutionCompletedPacket(final boolean isSuspended, final int fetchedRows) {
        if (isSuspended) {
            suspendPortal();
            return new PostgreSQLPortalSuspendedPacket();
        }
        if (getSqlStatement() instanceof EmptyStatement) {
            return new PostgreSQLEmptyQueryResponsePacket();
        }
        String sqlCommand = PostgreSQLCommand.valueOf(getSqlStatement().getClass()).map(PostgreSQLCommand::getTag).orElse("");
        return new PostgreSQLCommandCompletePacket(sqlCommand, Math.max(fetchedRows, getUpdateCount()));
    }
    
    private void suspendPortal() {
        backendConnection.markResourceInUse(databaseCommunicationEngine);
    }
    
    private long getUpdateCount() {
        return responseHeader instanceof UpdateResponseHeader ? ((UpdateResponseHeader) responseHeader).getUpdateCount() : 0;
    }
    
    @SneakyThrows(SQLException.class)
    @Override
    public void close() {
        if (null != databaseCommunicationEngine) {
            backendConnection.unmarkResourceInUse(databaseCommunicationEngine);
        }
        if (null != textProtocolBackendHandler) {
            textProtocolBackendHandler.close();
        }
    }
}
