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

package org.apache.shardingsphere.shardingproxy.backend.handler;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.shardingproxy.backend.ResultPacket;
import org.apache.shardingsphere.shardingproxy.backend.engine.DatabaseAccessEngineFactory;
import org.apache.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;

import java.util.LinkedList;
import java.util.List;

/**
 * Backend handler for broadcast.
 *
 * @author chenqingyang
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class BroadcastBackendHandler implements BackendHandler {
    
    private final DatabaseAccessEngineFactory databaseAccessEngineFactory = DatabaseAccessEngineFactory.getInstance();
    
    private final int sequenceId;
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    private final DatabaseType databaseType;
    
    @Override
    public CommandResponsePackets execute() {
        List<DatabasePacket> packets = new LinkedList<>();
        for (String each : GlobalRegistry.getInstance().getSchemaNames()) {
            packets.addAll(
                    databaseAccessEngineFactory.newTextProtocolInstance(GlobalRegistry.getInstance().getLogicSchema(each), sequenceId, sql, backendConnection, databaseType).execute().getPackets());
        }
        for (DatabasePacket each : packets) {
            if (each instanceof ErrPacket) {
                return new CommandResponsePackets(each);
            }
        }
        return new CommandResponsePackets(new OKPacket(1, 0, 0));
    }
    
    @Override
    public boolean next() {
        return false;
    }
    
    @Override
    public ResultPacket getResultValue() {
        return null;
    }
}
