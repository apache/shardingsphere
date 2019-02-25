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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.query;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandlerFactory;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLQueryCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.MySQLTextResultSetRowPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacket;

import java.sql.SQLException;

/**
 * MySQL COM_QUERY command packet.
 *
 * @author zhangliang
 * @author linjiaqi
 * @author zhaojun
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query.html">COM_QUERY</a>
 */
@Slf4j
public final class MySQLComPacketQuery implements MySQLQueryCommandPacket {
    
    @Getter
    private final int sequenceId;
    
    private final String sql;
    
    private final TextProtocolBackendHandler textProtocolBackendHandler;
    
    public MySQLComPacketQuery(final int sequenceId, final MySQLPacketPayload payload, final BackendConnection backendConnection) {
        this.sequenceId = sequenceId;
        sql = payload.readStringEOF();
        textProtocolBackendHandler = TextProtocolBackendHandlerFactory.newInstance(sql, backendConnection);
    }
    
    public MySQLComPacketQuery(final int sequenceId, final String sql) {
        this.sequenceId = sequenceId;
        this.sql = sql;
        textProtocolBackendHandler = null;
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(MySQLCommandPacketType.COM_QUERY.getValue());
        payload.writeStringEOF(sql);
    }
    
    @Override
    public Optional<CommandResponsePackets> execute() {
        log.debug("COM_QUERY received for Sharding-Proxy: {}", sql);
        return GlobalRegistry.getInstance().isCircuitBreak()
                ? Optional.of(new CommandResponsePackets(new MySQLErrPacket(1, MySQLServerErrorCode.ER_CIRCUIT_BREAK_MODE))) : Optional.of(textProtocolBackendHandler.execute());
    }
    
    @Override
    public boolean next() throws SQLException {
        return textProtocolBackendHandler.next();
    }
    
    @Override
    public DatabasePacket getQueryData() throws SQLException {
        QueryData queryData = textProtocolBackendHandler.getQueryData();
        return new MySQLTextResultSetRowPacket(queryData.getSequenceId(), queryData.getData());
    }
}
