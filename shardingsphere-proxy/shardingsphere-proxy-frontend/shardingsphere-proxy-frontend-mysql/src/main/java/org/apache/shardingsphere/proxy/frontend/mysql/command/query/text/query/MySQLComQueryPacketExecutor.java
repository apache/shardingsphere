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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.query;

import lombok.Getter;
import org.apache.shardingsphere.db.protocol.error.CommonErrorCode;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLColumnFieldDetailFlag;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLFieldCountPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.MySQLTextResultSetRowPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandlerFactory;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * COM_QUERY command packet executor for MySQL.
 */
public final class MySQLComQueryPacketExecutor implements QueryCommandExecutor {
    
    private final TextProtocolBackendHandler textProtocolBackendHandler;
    
    @Getter
    private volatile ResponseType responseType;
    
    private int currentSequenceId;
    
    public MySQLComQueryPacketExecutor(final MySQLComQueryPacket comQueryPacket, final BackendConnection backendConnection) {
        textProtocolBackendHandler = TextProtocolBackendHandlerFactory.newInstance(DatabaseTypes.getActualDatabaseType("MySQL"), comQueryPacket.getSql(), backendConnection);
    }
    
    @Override
    public Collection<DatabasePacket<?>> execute() throws SQLException {
        if (ProxyContext.getInstance().getSchemaContexts().isCircuitBreak()) {
            return Collections.singletonList(new MySQLErrPacket(1, CommonErrorCode.CIRCUIT_BREAK_MODE));
        }
        BackendResponse backendResponse = textProtocolBackendHandler.execute();
        if (backendResponse instanceof QueryResponse) {
            responseType = ResponseType.QUERY;
            return createQueryPackets((QueryResponse) backendResponse);
        }
        responseType = ResponseType.UPDATE;
        return Collections.singletonList(createUpdatePacket((UpdateResponse) backendResponse));
    }
    
    private Collection<DatabasePacket<?>> createQueryPackets(final QueryResponse backendResponse) {
        Collection<DatabasePacket<?>> result = new LinkedList<>();
        List<QueryHeader> queryHeader = backendResponse.getQueryHeaders();
        result.add(new MySQLFieldCountPacket(++currentSequenceId, queryHeader.size()));
        for (QueryHeader each : queryHeader) {
            result.add(new MySQLColumnDefinition41Packet(++currentSequenceId, getColumnFieldDetailFlag(each), each.getSchema(), each.getTable(), each.getTable(), 
                    each.getColumnLabel(), each.getColumnName(), each.getColumnLength(), MySQLColumnType.valueOfJDBCType(each.getColumnType()), each.getDecimals()));
        }
        result.add(new MySQLEofPacket(++currentSequenceId));
        return result;
    }

    private int getColumnFieldDetailFlag(final QueryHeader header) {
        int result = 0;
        if (header.isPrimaryKey()) {
            result += MySQLColumnFieldDetailFlag.PRIMARY_KEY.getValue();
        }
        if (header.isNotNull()) {
            result += MySQLColumnFieldDetailFlag.NOT_NULL.getValue();
        }
        if (!header.isSigned()) {
            result += MySQLColumnFieldDetailFlag.UNSIGNED.getValue();
        }
        if (header.isAutoIncrement()) {
            result += MySQLColumnFieldDetailFlag.AUTO_INCREMENT.getValue();
        }
        return result;
    }
    
    private MySQLOKPacket createUpdatePacket(final UpdateResponse updateResponse) {
        return new MySQLOKPacket(1, updateResponse.getUpdateCount(), updateResponse.getLastInsertId());
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
