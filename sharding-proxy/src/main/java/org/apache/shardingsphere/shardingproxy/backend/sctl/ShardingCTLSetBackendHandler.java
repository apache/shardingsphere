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

package org.apache.shardingsphere.shardingproxy.backend.sctl;

import com.google.common.base.Optional;
import org.apache.shardingsphere.shardingproxy.backend.ResultPacket;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.generic.DatabaseFailurePacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.generic.DatabaseSuccessPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.transaction.core.TransactionType;

/**
 * Sharding CTL backend handler.
 *
 * @author zhaojun
 */
public final class ShardingCTLSetBackendHandler implements TextProtocolBackendHandler {
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    public ShardingCTLSetBackendHandler(final String sql, final BackendConnection backendConnection) {
        this.sql = sql.toUpperCase().trim();
        this.backendConnection = backendConnection;
    }
    
    @Override
    public CommandResponsePackets execute() {
        Optional<ShardingCTLSetStatement> shardingTCLStatement = new ShardingCTLSetParser(sql).doParse();
        if (!shardingTCLStatement.isPresent()) {
            return new CommandResponsePackets(new DatabaseFailurePacket(1, 0, "", " please review your sctl format, should be sctl:set xxx=yyy."));
        }
        switch (shardingTCLStatement.get().getKey()) {
            case "TRANSACTION_TYPE":
                try {
                    backendConnection.setTransactionType(TransactionType.valueOf(shardingTCLStatement.get().getValue()));
                } catch (final IllegalArgumentException ex) {
                    return new CommandResponsePackets(new DatabaseFailurePacket(1, 0, "", String.format(" could not support this sctl grammar [%s].", sql)));
                }
                break;
            default:
                return new CommandResponsePackets(new DatabaseFailurePacket(1, 0, "", String.format(" could not support this sctl grammar [%s].", sql)));
        }
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
