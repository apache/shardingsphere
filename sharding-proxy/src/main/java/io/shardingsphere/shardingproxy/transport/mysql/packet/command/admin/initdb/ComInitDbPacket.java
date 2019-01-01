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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.initdb;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.frontend.mysql.CommandExecutor;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.runtime.RuntimeContext;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ServerErrorCode;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandPacketType;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * COM_INIT_DB command packet.
 *
 * @author zhangliang
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-init-db.html#packet-COM_INIT_DB">COM_INIT_DB</a>
 */
@Slf4j
public final class ComInitDbPacket implements CommandPacket {
    
    private static final GlobalRegistry GLOBAL_REGISTRY = GlobalRegistry.getInstance();
    
    private static final RuntimeContext RUNTIME_CONTEXT = RuntimeContext.getInstance();
    
    @Getter
    private final int sequenceId;
    
    private final String schema;
    
    private final BackendConnection backendConnection;
    
    public ComInitDbPacket(final int sequenceId, final MySQLPacketPayload payload, final BackendConnection backendConnection) {
        this.sequenceId = sequenceId;
        schema = payload.readStringEOF();
        this.backendConnection = backendConnection;
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(CommandPacketType.COM_INIT_DB.getValue());
        payload.writeStringEOF(schema);
    }
    
    @Override
    public Optional<CommandResponsePackets> execute() {
        log.debug("Schema name received for Sharding-Proxy: {}", schema);
        CommandResponsePackets commandResponsePackets;
        if (GlobalRegistry.getInstance().schemaExists(schema)) {
            backendConnection.setCurrentSchema(schema);
            commandResponsePackets = new CommandResponsePackets(new OKPacket(getSequenceId() + 1));
        } else {
            commandResponsePackets = new CommandResponsePackets(new ErrPacket(getSequenceId() + 1, ServerErrorCode.ER_BAD_DB_ERROR, schema));
        }
        if (GLOBAL_REGISTRY.getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.PROXY_BACKEND_USE_NIO)) {
            CommandExecutor commandExecutor = RUNTIME_CONTEXT.getUniqueCommandExecutor().get(RUNTIME_CONTEXT.getCommandPacketId().get());
            commandExecutor.writeResult(commandResponsePackets);
            return Optional.of(commandResponsePackets);
        } else {
            return Optional.of(commandResponsePackets);
        }
    }
}
