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
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;

import java.sql.SQLException;

/**
 * Backend handler for schema unicast.
 * 
 * @author chenqingyang
 */
public final class SchemaUnicastBackendHandler implements BackendHandler {
    
    private final BackendHandler backendHandler;
    
    public SchemaUnicastBackendHandler(final int connectionId, final int sequenceId, final String sql, final BackendConnection backendConnection, final DatabaseType databaseType) {
        String schema = GlobalRegistry.getInstance().getSchemaNames().iterator().next();
        backendHandler = BackendHandlerFactory.newTextProtocolInstance(connectionId, sequenceId, sql, backendConnection, databaseType, schema);
    }
    
    @Override
    public CommandResponsePackets execute() {
        return backendHandler.execute();
    }
    
    @Override
    public boolean next() throws SQLException {
        return backendHandler.next();
    }
    
    @Override
    public ResultPacket getResultValue() throws SQLException {
        return backendHandler.getResultValue();
    }
}
