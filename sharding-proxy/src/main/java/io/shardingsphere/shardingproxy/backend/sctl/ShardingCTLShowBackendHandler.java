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

package io.shardingsphere.shardingproxy.backend.sctl;

import com.google.common.base.Optional;
import io.shardingsphere.shardingproxy.backend.AbstractBackendHandler;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;

/**
 * Sharding CTL show backend handler.
 *
 * @author zhaojun
 */
public final class ShardingCTLShowBackendHandler extends AbstractBackendHandler {
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    public ShardingCTLShowBackendHandler(final String sql, final BackendConnection backendConnection) {
        this.sql = sql.toUpperCase().trim();
        this.backendConnection = backendConnection;
    }
    
    @Override
    protected CommandResponsePackets execute0() {
        Optional<ShardingCTLShowStatement> showStatement = new ShardingCTLShowParser(sql).doParse();
        if (!showStatement.isPresent()) {
            return new CommandResponsePackets(new ErrPacket(" please review your sctl format, should be sctl:show xxxx."));
        }
        switch (showStatement.get().getValue()) {
            case "TRANSACTION_TYPE":
                return new CommandResponsePackets(new OKPacket(String.format(" current transaction type is: %s", backendConnection.getTransactionType().name())));
            case "CACHED_CONNECTIONS":
                return new CommandResponsePackets(new OKPacket(String.format(" current channel cached connection size is: %s", backendConnection.getConnectionSize())));
            default:
                return new CommandResponsePackets(new ErrPacket(String.format(" could not support this sctl grammar [%s].", sql)));
        }
    }
}
