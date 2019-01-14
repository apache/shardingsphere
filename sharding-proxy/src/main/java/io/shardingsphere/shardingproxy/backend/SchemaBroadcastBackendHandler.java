/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Backend handler for schema broadcast.
 *
 * @author chenqingyang
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class SchemaBroadcastBackendHandler extends AbstractBackendHandler {
    
    private final int sequenceId;
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    private final DatabaseType databaseType;
    
    private final BackendHandlerFactory backendHandlerFactory;
    
    @Override
    protected CommandResponsePackets execute0() {
        List<DatabasePacket> packets = new LinkedList<>();
        String originSchemaName = backendConnection.getSchemaName();
        for (String each : GlobalRegistry.getInstance().getSchemaNames()) {
            backendConnection.setCurrentSchema(each);
            CommandResponsePackets responsePackets = backendHandlerFactory.newTextProtocolInstance(sequenceId, sql, backendConnection, databaseType).execute();
            packets.addAll(responsePackets.getPackets());
        }
        backendConnection.setCurrentSchema(originSchemaName);
        return merge(packets);
    }
    
    private CommandResponsePackets merge(final Collection<DatabasePacket> packets) {
        int affectedRows = 0;
        long lastInsertId = 0;
        for (DatabasePacket each : packets) {
            if (each instanceof ErrPacket) {
                return new CommandResponsePackets(each);
            }
            if (each instanceof OKPacket) {
                affectedRows += ((OKPacket) each).getAffectedRows();
                if (((OKPacket) each).getLastInsertId() > lastInsertId) {
                    lastInsertId = ((OKPacket) each).getLastInsertId();
                }
            }
        }
        return new CommandResponsePackets(new OKPacket(1, affectedRows, lastInsertId));
    }
}
