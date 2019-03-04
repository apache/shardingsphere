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

import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandlerFactory;
import org.apache.shardingsphere.shardingproxy.context.GlobalContext;
import org.apache.shardingsphere.shardingproxy.error.CommonErrorCode;
import org.apache.shardingsphere.shardingproxy.transport.api.packet.CommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.QueryCommandPacketExecutor;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLFieldCountPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.MySQLTextResultSetRowPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacketFactory;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLOKPacket;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * MySQL COM_QUERY command packet executor.
 *
 * @author zhangliang
 */
public final class MySQLComQueryPacketExecutor implements QueryCommandPacketExecutor<MySQLPacket> {
    
    private TextProtocolBackendHandler textProtocolBackendHandler;
    
    private boolean isQuery;
    
    private int currentSequenceId;
    
    @Override
    public Collection<MySQLPacket> execute(final BackendConnection backendConnection, final CommandPacket commandPacket) {
        MySQLComQueryPacket comQueryPacket = (MySQLComQueryPacket) commandPacket;
        textProtocolBackendHandler = TextProtocolBackendHandlerFactory.newInstance(comQueryPacket.getSql(), backendConnection);
        if (GlobalContext.getInstance().isCircuitBreak()) {
            return Collections.<MySQLPacket>singletonList(new MySQLErrPacket(1, CommonErrorCode.CIRCUIT_BREAK_MODE));
        }
        BackendResponse backendResponse = textProtocolBackendHandler.execute();
        if (backendResponse instanceof ErrorResponse) {
            return Collections.<MySQLPacket>singletonList(createErrorPacket(((ErrorResponse) backendResponse).getCause()));
        }
        if (backendResponse instanceof UpdateResponse) {
            return Collections.<MySQLPacket>singletonList(createUpdatePacket((UpdateResponse) backendResponse));
        }
        isQuery = true;
        return createQueryPackets((QueryResponse) backendResponse);
    }
    
    private MySQLErrPacket createErrorPacket(final Exception cause) {
        return MySQLErrPacketFactory.newInstance(1, cause);
    }
    
    private MySQLOKPacket createUpdatePacket(final UpdateResponse updateResponse) {
        return new MySQLOKPacket(1, updateResponse.getUpdateCount(), updateResponse.getLastInsertId());
    }
    
    private Collection<MySQLPacket> createQueryPackets(final QueryResponse backendResponse) {
        Collection<MySQLPacket> result = new LinkedList<>();
        List<QueryHeader> queryHeader = backendResponse.getQueryHeaders();
        result.add(new MySQLFieldCountPacket(++currentSequenceId, queryHeader.size()));
        for (QueryHeader each : queryHeader) {
            result.add(new MySQLColumnDefinition41Packet(++currentSequenceId, each));
        }
        result.add(new MySQLEofPacket(++currentSequenceId));
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
    public MySQLPacket getQueryData() throws SQLException {
        return new MySQLTextResultSetRowPacket(++currentSequenceId, textProtocolBackendHandler.getQueryData().getData());
    }
}
