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

package org.apache.shardingsphere.shardingproxy.backend.text.query;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.result.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryHeaderResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.query.ResultPacket;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.DataHeaderPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.QueryResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.generic.DatabaseFailurePacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLServerErrorCode;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Backend handler with query.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class QueryBackendHandler implements TextProtocolBackendHandler {
    
    private final DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory = DatabaseCommunicationEngineFactory.getInstance();
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Override
    public CommandResponsePackets execute() {
        if (null == backendConnection.getLogicSchema()) {
            return new CommandResponsePackets(new DatabaseFailurePacket(1, MySQLServerErrorCode.ER_NO_DB_ERROR));
        }
        databaseCommunicationEngine = databaseCommunicationEngineFactory.newTextProtocolInstance(backendConnection.getLogicSchema(), sql, backendConnection);
        BackendResponse backendResponse = databaseCommunicationEngine.execute();
        if (!(backendResponse instanceof QueryHeaderResponse)) {
            return new CommandResponsePackets(backendResponse.getHeadPacket());
        }
        QueryHeaderResponse headerResponse = (QueryHeaderResponse) backendResponse;
        Collection<DataHeaderPacket> dataHeaderPackets = new ArrayList<>(headerResponse.getQueryHeaders().size());
        for (QueryHeader each : headerResponse.getQueryHeaders()) {
            dataHeaderPackets.add(
                    new DataHeaderPacket(each.getSequenceId(), each.getSchema(), each.getTable(), each.getOrgTable(), each.getName(), each.getOrgName(),
                            each.getColumnLength(), each.getColumnType(), each.getDecimals()));
        }
        return new QueryResponsePackets(headerResponse.getColumnTypes(), headerResponse.getFieldCount(), dataHeaderPackets, headerResponse.getSequenceId());
    }
    
    @Override
    public boolean next() throws SQLException {
        return databaseCommunicationEngine.next();
    }
    
    @Override
    public ResultPacket getResultValue() throws SQLException {
        return databaseCommunicationEngine.getResultValue();
    }
}
