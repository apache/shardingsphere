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

package org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.text;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandlerFactory;
import org.apache.shardingsphere.shardingproxy.context.GlobalContext;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.CommandTransportResponse;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.TransportResponse;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.PostgreSQLQueryCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.apache.shardingsphere.shardingproxy.transport.spi.DatabasePacket;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * PostgreSQL command query packet.
 *
 * @author zhangyonglun
 */
@Slf4j
public final class PostgreSQLComQueryPacket implements PostgreSQLQueryCommandPacket {
    
    @Getter
    private final char messageType = PostgreSQLCommandPacketType.QUERY.getValue();
    
    private final String sql;
    
    private final TextProtocolBackendHandler textProtocolBackendHandler;
    
    private boolean isQuery;
    
    public PostgreSQLComQueryPacket(final PostgreSQLPacketPayload payload, final BackendConnection backendConnection) {
        payload.readInt4();
        sql = payload.readStringNul();
        textProtocolBackendHandler = TextProtocolBackendHandlerFactory.newInstance(sql, backendConnection);
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
    }
    
    @Override
    public Optional<TransportResponse> execute() {
        log.debug("PostgreSQLComQueryPacket received for Sharding-Proxy: {}", sql);
        if (GlobalContext.getInstance().isCircuitBreak()) {
            return Optional.<TransportResponse>of(new CommandTransportResponse(new PostgreSQLErrorResponsePacket()));
        }
        BackendResponse backendResponse = textProtocolBackendHandler.execute();
        if (backendResponse instanceof ErrorResponse) {
            return Optional.<TransportResponse>of(createErrorTransportResponse((ErrorResponse) backendResponse));
        }
        if (backendResponse instanceof UpdateResponse) {
            return Optional.<TransportResponse>of(createUpdateTransportResponse((UpdateResponse) backendResponse));
        }
        CommandTransportResponse result = createQueryTransportResponse((QueryResponse) backendResponse);
        return result.getPackets().isEmpty() ? Optional.<TransportResponse>absent() : Optional.<TransportResponse>of(result);
    }
    
    private CommandTransportResponse createErrorTransportResponse(final ErrorResponse errorResponse) {
        return new CommandTransportResponse(new PostgreSQLErrorResponsePacket());
    }
    
    private CommandTransportResponse createUpdateTransportResponse(final UpdateResponse updateResponse) {
        return new CommandTransportResponse(new PostgreSQLCommandCompletePacket());
    }
    
    private CommandTransportResponse createQueryTransportResponse(final QueryResponse queryResponse) {
        List<PostgreSQLColumnDescription> postgreSQLColumnDescriptions = getPostgreSQLColumnDescriptions(queryResponse);
        isQuery = !postgreSQLColumnDescriptions.isEmpty();
        if (postgreSQLColumnDescriptions.isEmpty()) {
            return new CommandTransportResponse();
        }
        return new CommandTransportResponse(new PostgreSQLRowDescriptionPacket(postgreSQLColumnDescriptions.size(), postgreSQLColumnDescriptions));
    }
    
    private List<PostgreSQLColumnDescription> getPostgreSQLColumnDescriptions(final QueryResponse queryResponse) {
        List<PostgreSQLColumnDescription> result = new LinkedList<>();
        int columnIndex = 0;
        for (QueryHeader each : queryResponse.getQueryHeaders()) {
            result.add(new PostgreSQLColumnDescription(each, ++columnIndex));
        }
        return result;
    }
    
    @Override
    public boolean isQuery() {
        return isQuery;
    }
    
    @Override
    public boolean next() throws SQLException {
        return textProtocolBackendHandler.next();
    }
    
    @Override
    public DatabasePacket getQueryData() throws SQLException {
        return new PostgreSQLDataRowPacket(textProtocolBackendHandler.getQueryData().getData());
    }
}
