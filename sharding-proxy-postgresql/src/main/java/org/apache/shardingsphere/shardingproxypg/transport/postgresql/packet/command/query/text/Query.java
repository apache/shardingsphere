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

package org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.query.text;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.shardingproxypg.backend.ResultPacket;
import org.apache.shardingsphere.shardingproxypg.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxypg.backend.text.ComQueryBackendHandlerFactory;
import org.apache.shardingsphere.shardingproxypg.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.shardingproxypg.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxypg.transport.common.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.PostgreSQLPacketPayload;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.PostgreSQLCommandResponsePackets;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.query.PostgreSQLQueryCommandPacket;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.generic.ErrorResponse;

import java.sql.SQLException;

/**
 * Query message.
 *
 * @author zhangyonglun
 */
@Slf4j
public final class Query implements PostgreSQLQueryCommandPacket {
    
    @Getter
    private final char messageType = PostgreSQLCommandPacketType.QUERY.getValue();
    
    private final String sql;
    
    private final TextProtocolBackendHandler textProtocolBackendHandler;
    
    public Query(final PostgreSQLPacketPayload payload, final BackendConnection backendConnection) {
        payload.readInt4();
        sql = payload.readStringNul();
        textProtocolBackendHandler = ComQueryBackendHandlerFactory.createTextProtocolBackendHandler(0, sql, backendConnection, DatabaseType.PostgreSQL);
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
    }
    
    @Override
    public Optional<PostgreSQLCommandResponsePackets> execute() {
        log.debug("QUERY received for Sharding-Proxy: {}", sql);
        return GlobalRegistry.getInstance().isCircuitBreak()
                ? Optional.of(new PostgreSQLCommandResponsePackets(new ErrorResponse())) : Optional.of(textProtocolBackendHandler.execute());
    }
    
    @Override
    public boolean next() throws SQLException {
        return textProtocolBackendHandler.next();
    }
    
    @Override
    public DatabasePacket getResultValue() throws SQLException {
        ResultPacket resultPacket = textProtocolBackendHandler.getResultValue();
        return new PostgreSQLDataRowPacket(resultPacket.getData());
    }
    
    @Override
    public int getSequenceId() {
        return 0;
    }
}
