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
import io.shardingsphere.transaction.api.TransactionType;

/**
 * Sharding CTL backend handler.
 *
 * @author zhaojun
 */
public final class ShardingCTLSetBackendHandler extends AbstractBackendHandler {
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    public ShardingCTLSetBackendHandler(final String sql, final BackendConnection backendConnection) {
        this.sql = sql.toUpperCase().trim();
        this.backendConnection = backendConnection;
    }
    
    @Override
    protected CommandResponsePackets execute0() {
        Optional<ShardingCTLSetStatement> shardingTCLStatement = new ShardingCTLSetParser(sql).doParse();
        if (!shardingTCLStatement.isPresent()) {
            return new CommandResponsePackets(new ErrPacket(" please review your sctl format, should be sctl:set xxx=yyy."));
        }
        switch (shardingTCLStatement.get().getKey()) {
            case "TRANSACTION_TYPE":
                backendConnection.setTransactionType(TransactionType.valueOf(shardingTCLStatement.get().getValue()));
                break;
            default:
                return new CommandResponsePackets(new ErrPacket(String.format(" could not support this sctl grammar [%s].", sql)));
        }
        return new CommandResponsePackets(new OKPacket(1));
    }
}
