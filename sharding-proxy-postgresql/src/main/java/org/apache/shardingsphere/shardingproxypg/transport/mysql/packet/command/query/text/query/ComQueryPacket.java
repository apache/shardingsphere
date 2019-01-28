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

package org.apache.shardingsphere.shardingproxypg.transport.mysql.packet.command.query.text.query;

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
import org.apache.shardingsphere.shardingproxypg.transport.mysql.constant.ServerErrorCode;
import org.apache.shardingsphere.shardingproxypg.transport.mysql.packet.MySQLPacketPayload;
import org.apache.shardingsphere.shardingproxypg.transport.mysql.packet.command.CommandPacketType;
import org.apache.shardingsphere.shardingproxypg.transport.mysql.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxypg.transport.mysql.packet.command.query.QueryCommandPacket;
import org.apache.shardingsphere.shardingproxypg.transport.mysql.packet.command.query.text.TextResultSetRowPacket;
import org.apache.shardingsphere.shardingproxypg.transport.mysql.packet.generic.ErrPacket;

import java.sql.SQLException;

/**
 * COM_QUERY command packet.
 *
 * @author zhangliang
 * @author linjiaqi
 * @author zhaojun
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query.html">COM_QUERY</a>
 */
@Slf4j
public final class ComQueryPacket implements QueryCommandPacket {
    
    @Getter
    private final int sequenceId;
    
    private final String sql;
    
    private final TextProtocolBackendHandler textProtocolBackendHandler;
    
    public ComQueryPacket(final int sequenceId, final MySQLPacketPayload payload, final BackendConnection backendConnection) {
        this.sequenceId = sequenceId;
        sql = payload.readStringEOF();
        textProtocolBackendHandler = ComQueryBackendHandlerFactory.createTextProtocolBackendHandler(sequenceId, sql, backendConnection, DatabaseType.MySQL);
    }
    
    public ComQueryPacket(final int sequenceId, final String sql) {
        this.sequenceId = sequenceId;
        this.sql = sql;
        textProtocolBackendHandler = null;
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(CommandPacketType.COM_QUERY.getValue());
        payload.writeStringEOF(sql);
    }
    
    @Override
    public Optional<CommandResponsePackets> execute() {
        log.debug("COM_QUERY received for Sharding-Proxy: {}", sql);
        return Optional.of(null);
    }
    
    @Override
    public boolean next() throws SQLException {
        return textProtocolBackendHandler.next();
    }
    
    @Override
    public DatabasePacket getResultValue() throws SQLException {
        ResultPacket resultPacket = textProtocolBackendHandler.getResultValue();
        return new TextResultSetRowPacket(resultPacket.getSequenceId(), resultPacket.getData());
    }
}
