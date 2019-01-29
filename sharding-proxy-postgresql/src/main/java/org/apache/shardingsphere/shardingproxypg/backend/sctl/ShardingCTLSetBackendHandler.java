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

package org.apache.shardingsphere.shardingproxypg.backend.sctl;

import com.google.common.base.Optional;
import org.apache.shardingsphere.shardingproxypg.backend.ResultPacket;
import org.apache.shardingsphere.shardingproxypg.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxypg.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.shardingproxypg.transport.mysql.packet.generic.OKPacket;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.PostgreSQLCommandResponsePackets;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.generic.ErrorResponsePacket;
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
    public PostgreSQLCommandResponsePackets execute() {
        Optional<ShardingCTLSetStatement> shardingTCLStatement = new ShardingCTLSetParser(sql).doParse();
        if (!shardingTCLStatement.isPresent()) {
            return new PostgreSQLCommandResponsePackets(new ErrorResponsePacket());
        }
        switch (shardingTCLStatement.get().getKey()) {
            case "TRANSACTION_TYPE":
                try {
                    backendConnection.setTransactionType(TransactionType.valueOf(shardingTCLStatement.get().getValue()));
                } catch (final IllegalArgumentException ex) {
                    return new PostgreSQLCommandResponsePackets(new ErrorResponsePacket());
                }
                break;
            default:
                return new PostgreSQLCommandResponsePackets(new ErrorResponsePacket());
        }
        return new PostgreSQLCommandResponsePackets(new OKPacket(1));
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
