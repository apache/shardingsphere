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
import org.apache.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;

import java.sql.SQLException;

/**
 * Unicast schema backend handler.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class UnicastSchemaBackendHandler extends AbstractBackendHandler {
    
    private final int sequenceId;
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    private final BackendHandlerFactory backendHandlerFactory;
    
    private BackendHandler delegate;
    
    @Override
    protected CommandResponsePackets execute0() {
        if (null == backendConnection.getSchemaName()) {
            backendConnection.setCurrentSchema(GlobalRegistry.getInstance().getSchemaNames().iterator().next());
        }
        delegate = backendHandlerFactory.newTextProtocolInstance(sequenceId, sql, backendConnection, DatabaseType.MySQL);
        return delegate.execute();
    }
    
    @Override
    public boolean next() throws SQLException {
        return delegate.next();
    }
    
    @Override
    public ResultPacket getResultValue() throws SQLException {
        return delegate.getResultValue();
    }
}
