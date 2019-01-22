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

import org.apache.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.jdbc.connection.BackendTransactionManager;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;

/**
 * Do transaction operation.
 *
 * @author zhaojun
 */
public final class TransactionBackendHandler extends AbstractBackendHandler {
    
    private final TransactionOperationType operationType;
    
    private final BackendTransactionManager backendTransactionManager;
    
    public TransactionBackendHandler(final TransactionOperationType operationType, final BackendConnection backendConnection) {
        this.operationType = operationType;
        backendTransactionManager = new BackendTransactionManager(backendConnection);
    }
    
    @Override
    protected CommandResponsePackets execute0() throws Exception {
        switch (operationType) {
            case BEGIN:
                backendTransactionManager.begin();
                break;
            case COMMIT:
                backendTransactionManager.commit();
                break;
            case ROLLBACK:
                backendTransactionManager.rollback();
                break;
            default:
                throw new UnsupportedOperationException(operationType.name());
        }
        return new CommandResponsePackets(new OKPacket(1));
    }
}
