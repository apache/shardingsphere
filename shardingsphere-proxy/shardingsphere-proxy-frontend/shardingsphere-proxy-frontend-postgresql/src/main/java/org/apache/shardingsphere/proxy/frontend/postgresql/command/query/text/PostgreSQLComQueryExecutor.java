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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.text;

import lombok.Getter;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.text.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.text.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandlerFactory;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Command query executor for PostgreSQL.
 */
public final class PostgreSQLComQueryExecutor implements QueryCommandExecutor {
    
    private final TextProtocolBackendHandler textProtocolBackendHandler;
    
    @Getter
    private volatile ResponseType responseType;
    
    public PostgreSQLComQueryExecutor(final PostgreSQLComQueryPacket comQueryPacket, final BackendConnection backendConnection) {
        textProtocolBackendHandler = TextProtocolBackendHandlerFactory.newInstance(DatabaseTypeRegistry.getActualDatabaseType("PostgreSQL"), comQueryPacket.getSql(), backendConnection);
    }
    
    @Override
    public Collection<DatabasePacket<?>> execute() throws SQLException {
        ResponseHeader responseHeader = textProtocolBackendHandler.execute();
        if (responseHeader instanceof QueryResponseHeader) {
            Optional<PostgreSQLRowDescriptionPacket> result = createQueryPacket((QueryResponseHeader) responseHeader);
            return result.<List<DatabasePacket<?>>>map(Collections::singletonList).orElseGet(Collections::emptyList);
        }
        responseType = ResponseType.UPDATE;
        return Collections.singletonList(createUpdatePacket((UpdateResponseHeader) responseHeader));
    }
    
    private Optional<PostgreSQLRowDescriptionPacket> createQueryPacket(final QueryResponseHeader queryResponseHeader) {
        Collection<PostgreSQLColumnDescription> columnDescriptions = createColumnDescriptions(queryResponseHeader);
        if (columnDescriptions.isEmpty()) {
            responseType = ResponseType.QUERY;
        }
        if (columnDescriptions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new PostgreSQLRowDescriptionPacket(columnDescriptions.size(), columnDescriptions));
    }
    
    private Collection<PostgreSQLColumnDescription> createColumnDescriptions(final QueryResponseHeader queryResponseHeader) {
        Collection<PostgreSQLColumnDescription> result = new LinkedList<>();
        int columnIndex = 0;
        for (QueryHeader each : queryResponseHeader.getQueryHeaders()) {
            result.add(new PostgreSQLColumnDescription(each.getColumnName(), ++columnIndex, each.getColumnType(), each.getColumnLength(), each.getColumnTypeName()));
        }
        return result;
    }
    
    private PostgreSQLCommandCompletePacket createUpdatePacket(final UpdateResponseHeader updateResponseHeader) {
        return new PostgreSQLCommandCompletePacket(updateResponseHeader.getType(), updateResponseHeader.getUpdateCount());
    }
    
    @Override
    public boolean next() throws SQLException {
        return textProtocolBackendHandler.next();
    }
    
    @Override
    public PostgreSQLPacket getQueryData() throws SQLException {
        return new PostgreSQLDataRowPacket(textProtocolBackendHandler.getRowData());
    }
}
