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

import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.UseStatement;
import io.shardingsphere.core.util.SQLUtil;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ServerErrorCode;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import lombok.RequiredArgsConstructor;

/**
 * Use schema backend handler.
 *
 * @author chenqingyang
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class UseSchemaBackendHandler extends AbstractBackendHandler {
    
    private final UseStatement useStatement;
    
    private final BackendConnection backendConnection;
    
    @Override
    protected CommandResponsePackets execute0() {
        String schema = SQLUtil.getExactlyValue(useStatement.getSchema());
        if (!GlobalRegistry.getInstance().schemaExists(schema)) {
            return new CommandResponsePackets(new ErrPacket(1, ServerErrorCode.ER_BAD_DB_ERROR, schema));
        }
        backendConnection.setCurrentSchema(schema);
        return new CommandResponsePackets(new OKPacket(1));
    }
}
