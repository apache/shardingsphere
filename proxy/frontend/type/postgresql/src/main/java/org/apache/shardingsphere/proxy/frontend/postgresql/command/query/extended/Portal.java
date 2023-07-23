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
import org.apache.shardingsphere.db.protocol.binary.BinaryCell;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLValueFormat;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLEmptyQueryResponsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLNoDataPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.protocol.util.PostgreSQLTextBitUtils;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.protocol.util.PostgreSQLTextBoolUtils;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLPortalSuspendedPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLParameterStatusPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.infra.binder.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.PostgreSQLCommand;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.EmptyStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * PostgreSQL portal.
 */
public final class Portal {
    
    @Getter
    private final String name;
    
    @Getter
    private final SQLStatement sqlStatement;
    
    private final List<PostgreSQLValueFormat> resultFormats;
    
    private final ProxyBackendHandler proxyBackendHandler;
    
    private final ProxyDatabaseConnectionManager databaseConnectionManager;
    
    private ResponseHeader responseHeader;
    
    public Portal(final String name, final PostgreSQLServerPreparedStatement preparedStatement, final List<Object> params, final List<PostgreSQLValueFormat> resultFormats,
                  final ProxyDatabaseConnectionManager databaseConnectionManager) throws SQLException {
        this.name = name;
        this.sqlStatement = preparedStatement.getSqlStatementContext().getSqlStatement();
        this.resultFormats = resultFormats;
        this.databaseConnectionManager = databaseConnectionManager;
        String databaseName = databaseConnectionManager.getConnectionSession().getDefaultDatabaseName();
        SQLStatementContext sqlStatementContext = preparedStatement.getSqlStatementContext();
        if (sqlStatementContext instanceof ParameterAware) {
            ((ParameterAware) sqlStatementContext).setUpParameters(params);
        }
        DatabaseType protocolType = ProxyContext.getInstance().getDatabase(databaseName).getProtocolType();
        QueryContext queryContext = new QueryContext(sqlStatementContext, preparedStatement.getSql(), params, preparedStatement.getHintValueContext(), true);
        databaseConnectionManager.getConnectionSession().setQueryContext(queryContext);
        proxyBackendHandler = ProxyBackendHandlerFactory.newInstance(protocolType, queryContext, databaseConnectionManager.getConnectionSession(), true);
    }
    
    /**
     * Do bind.
     * 
     * @throws SQLException SQL exception
     */
    public void bind() throws SQLException {
        responseHeader = proxyBackendHandler.execute();
    }
    
    /**
     * Describe portal.
     *
     * @return portal description packet
     * @throws IllegalStateException illegal state exception
     */
    public PostgreSQLPacket describe() {
        if (responseHeader instanceof QueryResponseHeader) {
            return createRowDescriptionPacket((QueryResponseHeader) responseHeader);
        }
        if (responseHeader instanceof UpdateResponseHeader) {
            return PostgreSQLNoDataPacket.getInstance();
        }
        throw new IllegalStateException(String.format("Can not describe portal `%s` before bind", name));
    }
    
    private PostgreSQLRowDescriptionPacket createRowDescriptionPacket(final QueryResponseHeader queryResponseHeader) {
        return new PostgreSQLRowDescriptionPacket(createColumnDescriptions(queryResponseHeader));
    }
    
    private Collection<PostgreSQLColumnDescription> createColumnDescriptions(final QueryResponseHeader queryResponseHeader) {
        Collection<PostgreSQLColumnDescription> result = new LinkedList<>();
        int columnIndex = 0;
        for (QueryHeader each : queryResponseHeader.getQueryHeaders()) {
            PostgreSQLValueFormat valueFormat = determineValueFormat(columnIndex);
            result.add(new PostgreSQLColumnDescription(each.getColumnLabel(), ++columnIndex, each.getColumnType(), each.getColumnLength(), each.getColumnTypeName(), valueFormat.getCode()));
        }
        return result;
    }
    
    /**
     * Execute portal.
     *
     * @param maxRows max rows of query result
     * @return execute result
     * @throws SQLException SQL exception
     */
    public List<DatabasePacket> execute(final int maxRows) throws SQLException {
        int fetchSize = maxRows > 0 ? maxRows : Integer.MAX_VALUE;
        List<DatabasePacket> result = new LinkedList<>();
        for (int i = 0; i < fetchSize && hasNext(); i++) {
            result.add(nextPacket());
        }
        if (responseHeader instanceof UpdateResponseHeader && sqlStatement instanceof SetStatement) {
            result.addAll(createParameterStatusResponse((SetStatement) sqlStatement));
            return result;
        }
        result.add(createExecutionCompletedPacket(maxRows > 0 && maxRows == result.size(), result.size()));
        return result;
    }
    
    private List<PostgreSQLPacket> createParameterStatusResponse(final SetStatement sqlStatement) {
        List<PostgreSQLPacket> result = new ArrayList<>(2);
        result.add(new PostgreSQLCommandCompletePacket("SET", 0));
        for (VariableAssignSegment each : sqlStatement.getVariableAssigns()) {
            result.add(new PostgreSQLParameterStatusPacket(each.getVariable().getVariable(), IdentifierValue.getQuotedContent(each.getAssignValue())));
        }
        return result;
    }
    
    private boolean hasNext() throws SQLException {
        return proxyBackendHandler.next();
    }
    
    private PostgreSQLPacket nextPacket() throws SQLException {
        return new PostgreSQLDataRowPacket(getData(proxyBackendHandler.getRowData()));
    }
    
    private List<Object> getData(final QueryResponseRow queryResponseRow) {
        Collection<QueryResponseCell> cells = queryResponseRow.getCells();
        List<Object> result = new ArrayList<>(cells.size());
        List<QueryResponseCell> columns = new ArrayList<>(cells);
        for (int i = 0; i < columns.size(); i++) {
            PostgreSQLValueFormat format = determineValueFormat(i);
            result.add(PostgreSQLValueFormat.BINARY == format ? createBinaryCell(columns.get(i)) : getCellData(columns.get(i)));
        }
        return result;
    }
    
    private PostgreSQLValueFormat determineValueFormat(final int columnIndex) {
        return resultFormats.isEmpty() ? PostgreSQLValueFormat.TEXT : resultFormats.get(columnIndex % resultFormats.size());
    }
    
    private BinaryCell createBinaryCell(final QueryResponseCell cell) {
        return new BinaryCell(PostgreSQLColumnType.valueOfJDBCType(cell.getJdbcType(), cell.getColumnTypeName().orElse(null)), getCellData(cell));
    }
    
    private Object getCellData(final QueryResponseCell cell) {
        if (PostgreSQLColumnType.isBit(cell.getJdbcType(), cell.getColumnTypeName().orElse(null))) {
            return PostgreSQLTextBitUtils.getTextValue(cell.getData());
        }
        if (PostgreSQLColumnType.isBool(cell.getJdbcType(), cell.getColumnTypeName().orElse(null))) {
            return PostgreSQLTextBoolUtils.getTextValue(cell.getData());
        }
        return cell.getData();
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
        databaseConnectionManager.markResourceInUse(proxyBackendHandler);
    }
    
    private long getUpdateCount() {
        return responseHeader instanceof UpdateResponseHeader ? ((UpdateResponseHeader) responseHeader).getUpdateCount() : 0;
    }
    
    /**
     * Close portal.
     *
     * @throws SQLException SQL exception
     */
    public void close() throws SQLException {
        databaseConnectionManager.unmarkResourceInUse(proxyBackendHandler);
        proxyBackendHandler.close();
    }
}
