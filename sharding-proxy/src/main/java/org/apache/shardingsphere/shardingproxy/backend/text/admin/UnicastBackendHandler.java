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

package org.apache.shardingsphere.shardingproxy.backend.text.admin;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.result.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryHeaderResponse;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.DataHeaderPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.QueryResponsePackets;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Backend handler for unicast.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class UnicastBackendHandler implements TextProtocolBackendHandler {
    
    private final DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory = DatabaseCommunicationEngineFactory.getInstance();
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Override
    public CommandResponsePackets execute() {
        databaseCommunicationEngine = databaseCommunicationEngineFactory.newTextProtocolInstance(GlobalRegistry.getInstance().getLogicSchemas().values().iterator().next(), sql, backendConnection);
        BackendResponse backendResponse = databaseCommunicationEngine.execute();
        if (backendResponse instanceof QueryHeaderResponse) {
            QueryHeaderResponse headerResponse = (QueryHeaderResponse) backendResponse;
            Collection<DataHeaderPacket> dataHeaderPackets = new ArrayList<>(headerResponse.getQueryHeaders().size());
            int sequenceId = 1;
            for (QueryHeader each : headerResponse.getQueryHeaders()) {
                dataHeaderPackets.add(new DataHeaderPacket(++sequenceId, each.getSchema(), each.getTable(), each.getTable(), 
                        each.getColumnLabel(), each.getColumnName(), each.getColumnLength(), each.getColumnType(), each.getDecimals()));
            }
            return new QueryResponsePackets(dataHeaderPackets, headerResponse.getSequenceId());
        }
        return new CommandResponsePackets(databaseCommunicationEngine.execute().getPackets());
    }
    
    @Override
    public boolean next() throws SQLException {
        return databaseCommunicationEngine.next();
    }
    
    @Override
    public QueryData getQueryData() throws SQLException {
        return databaseCommunicationEngine.getQueryData();
    }
}
