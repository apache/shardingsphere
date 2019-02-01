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
import org.apache.shardingsphere.core.parsing.parser.dialect.mysql.statement.UseStatement;
import org.apache.shardingsphere.core.util.SQLUtil;
import org.apache.shardingsphere.shardingproxy.backend.ResultPacket;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.generic.DatabaseSuccessPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.ServerErrorCode;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;

/**
 * Use database backend handler.
 *
 * @author chenqingyang
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class UseDatabaseBackendHandler implements TextProtocolBackendHandler {
    
    private final UseStatement useStatement;
    
    private final BackendConnection backendConnection;
    
    @Override
    public CommandResponsePackets execute() {
        String schema = SQLUtil.getExactlyValue(useStatement.getSchema());
        if (!GlobalRegistry.getInstance().schemaExists(schema)) {
            ServerErrorCode serverErrorCode = ServerErrorCode.ER_BAD_DB_ERROR;
            return new CommandResponsePackets(new ErrPacket(1, serverErrorCode.getErrorCode(), serverErrorCode.getSqlState(), String.format(serverErrorCode.getErrorMessage(), schema)));
        }
        backendConnection.setCurrentSchema(schema);
        return new CommandResponsePackets(new DatabaseSuccessPacket(1, 0L, 0L));
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
