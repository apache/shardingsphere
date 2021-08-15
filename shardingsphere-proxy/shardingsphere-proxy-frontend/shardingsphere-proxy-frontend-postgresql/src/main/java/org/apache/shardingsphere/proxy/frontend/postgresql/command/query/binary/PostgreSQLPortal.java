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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary;

import lombok.Getter;
import org.apache.shardingsphere.db.protocol.binary.BinaryCell;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLValueFormat;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.text.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.data.impl.BinaryQueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandlerFactory;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * PostgreSQL portal.
 */
@Getter
public final class PostgreSQLPortal {
    
    private final SQLStatement sqlStatement;
    
    private final List<PostgreSQLValueFormat> resultFormats;
    
    private final DatabaseCommunicationEngine databaseCommunicationEngine;
    
    private final TextProtocolBackendHandler textProtocolBackendHandler;
    
    private final BackendConnection backendConnection;
    
    public PostgreSQLPortal(final PostgreSQLBinaryStatement binaryStatement, final List<Object> parameters, final List<PostgreSQLValueFormat> resultFormats,
                            final BackendConnection backendConnection) throws SQLException {
        this.sqlStatement = binaryStatement.getSqlStatement();
        this.resultFormats = resultFormats;
        this.backendConnection = backendConnection;
        if (sqlStatement instanceof TCLStatement || sqlStatement instanceof EmptyStatement) {
            databaseCommunicationEngine = null;
            textProtocolBackendHandler = 
                    TextProtocolBackendHandlerFactory.newInstance(DatabaseTypeRegistry.getActualDatabaseType("PostgreSQL"), binaryStatement.getSql(), backendConnection);
            return;
        }
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(
                ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataMap(), parameters, sqlStatement, backendConnection.getDefaultSchemaName());
        databaseCommunicationEngine = DatabaseCommunicationEngineFactory.getInstance().newBinaryProtocolInstance(sqlStatementContext, 
                binaryStatement.getSql(), parameters, backendConnection);
        textProtocolBackendHandler = null;
    }
    
    /**
     * Execute portal.
     *
     * @return response header
     * @throws SQLException SQL exception
     */
    public ResponseHeader execute() throws SQLException {
        return null != databaseCommunicationEngine ? databaseCommunicationEngine.execute() : textProtocolBackendHandler.execute();
    }
    
    /**
     * Next.
     *
     * @return true if the portal has next packet; else false
     * @throws SQLException SQL exception
     */
    public boolean next() throws SQLException {
        return null != databaseCommunicationEngine && databaseCommunicationEngine.next();
    }
    
    /**
     * Fetch next packet from the portal.
     *
     * @return next packet
     * @throws SQLException SQL exception
     */
    public PostgreSQLPacket nextPacket() throws SQLException {
        QueryResponseRow queryResponseRow = databaseCommunicationEngine.getQueryResponseRow();
        return new PostgreSQLDataRowPacket(getData(queryResponseRow));
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
        return new BinaryCell(PostgreSQLBinaryColumnType.valueOfJDBCType(((BinaryQueryResponseCell) cell).getJdbcType()), cell.getData());
    }
    
    /**
     * Suspend the portal.
     */
    public void suspend() {
        backendConnection.markResourceInUse(databaseCommunicationEngine);
    }
    
    /**
     * Close portal.
     *
     * @throws SQLException SQL exception
     */
    public void close() throws SQLException {
        if (null != databaseCommunicationEngine) {
            backendConnection.unmarkResourceInUse(databaseCommunicationEngine);
        }
        if (null != textProtocolBackendHandler) {
            textProtocolBackendHandler.close();
        }
    }
}
