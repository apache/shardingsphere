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

package org.apache.shardingsphere.proxy.backend.text.sctl.set;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.sctl.exception.InvalidShardingCTLFormatException;
import org.apache.shardingsphere.proxy.backend.text.sctl.exception.UnsupportedShardingCTLTypeException;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.util.Optional;

/**
 * Sharding CTL backend handler.
 */
public final class ShardingCTLSetBackendHandler implements TextProtocolBackendHandler {
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    public ShardingCTLSetBackendHandler(final String sql, final BackendConnection backendConnection) {
        this.sql = sql.toUpperCase().trim();
        this.backendConnection = backendConnection;
    }
    
    @Override
    public ResponseHeader execute() {
        Optional<ShardingCTLSetStatement> shardingTCLStatement = new ShardingCTLSetParser(sql).doParse();
        if (!shardingTCLStatement.isPresent()) {
            throw new InvalidShardingCTLFormatException(sql);
        }
        if ("TRANSACTION_TYPE".equals(shardingTCLStatement.get().getKey())) {
            if (null == backendConnection.getSchemaName()) {
                throw new ShardingSphereException("Please select database, then switch transaction type.");
            }
            try {
                backendConnection.getTransactionStatus().setTransactionType(TransactionType.valueOf(shardingTCLStatement.get().getValue()));
            } catch (final IllegalArgumentException ex) {
                throw new UnsupportedShardingCTLTypeException(sql);
            }
        } else {
            throw new UnsupportedShardingCTLTypeException(sql);
        }
        return new UpdateResponseHeader(null);
    }
}
