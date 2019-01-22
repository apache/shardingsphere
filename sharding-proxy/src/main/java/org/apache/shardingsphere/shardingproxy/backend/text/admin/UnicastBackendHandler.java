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
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.shardingproxy.backend.ResultPacket;
import org.apache.shardingsphere.shardingproxy.backend.engine.DatabaseAccessEngine;
import org.apache.shardingsphere.shardingproxy.backend.engine.DatabaseAccessEngineFactory;
import org.apache.shardingsphere.shardingproxy.backend.engine.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;

import java.sql.SQLException;

/**
 * Backend handler for unicast.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class UnicastBackendHandler implements TextProtocolBackendHandler {
    
    private final DatabaseAccessEngineFactory databaseAccessEngineFactory = DatabaseAccessEngineFactory.getInstance();
    
    private final int sequenceId;
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    private final DatabaseType databaseType;
    
    private DatabaseAccessEngine databaseAccessEngine;
    
    @Override
    public CommandResponsePackets execute() {
        databaseAccessEngine = databaseAccessEngineFactory.newTextProtocolInstance(
                GlobalRegistry.getInstance().getLogicSchemas().values().iterator().next(), sequenceId, sql, backendConnection, databaseType);
        return databaseAccessEngine.execute();
    }
    
    @Override
    public boolean next() throws SQLException {
        return databaseAccessEngine.next();
    }
    
    @Override
    public ResultPacket getResultValue() throws SQLException {
        return databaseAccessEngine.getResultValue();
    }
}
