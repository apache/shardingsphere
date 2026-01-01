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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction;

import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.constant.buffer.FirebirdParameterBuffer;
import org.apache.shardingsphere.database.protocol.firebird.constant.buffer.type.FirebirdTransactionParameterBufferType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;

/**
 * Firebird start transaction packet.
 */
@Getter
public final class FirebirdStartTransactionPacket extends FirebirdCommandPacket {
    
    private final int handle;
    
    private final FirebirdParameterBuffer tpb = FirebirdTransactionParameterBufferType.createBuffer();
    
    public FirebirdStartTransactionPacket(final FirebirdPacketPayload payload) {
        payload.skipReserved(4);
        handle = payload.readInt4();
        tpb.parseBuffer(payload.readBuffer());
    }
    
    /**
     * Is auto commit.
     *
     * @return auto commit or not
     */
    public boolean isAutoCommit() {
        return null != tpb.getValue(FirebirdTransactionParameterBufferType.AUTOCOMMIT);
    }
    
    /**
     * Is read only.
     *
     * @return read only or not
     */
    public boolean isReadOnly() {
        return null != tpb.getValue(FirebirdTransactionParameterBufferType.READ);
    }
    
    /**
     * Get transaction isolation level based on the transaction parameter buffer.
     *
     * @return transaction isolation level
     */
    public TransactionIsolationLevel getIsolationLevel() {
        if (null != tpb.getValue(FirebirdTransactionParameterBufferType.READ_COMMITTED)) {
            return TransactionIsolationLevel.READ_COMMITTED;
        }
        if (tpb.getValue(FirebirdTransactionParameterBufferType.CONCURRENCY)) {
            return TransactionIsolationLevel.REPEATABLE_READ;
        }
        if (tpb.getValue(FirebirdTransactionParameterBufferType.CONSISTENCY)) {
            return TransactionIsolationLevel.SERIALIZABLE;
        }
        return TransactionIsolationLevel.NONE;
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
    }
    
    /**
     * Get length of packet.
     *
     * @param payload Firebird packet payload
     * @return Length of packet
     */
    public static int getLength(final FirebirdPacketPayload payload) {
        int length = 8;
        length += payload.getBufferLength(length);
        return length;
    }
}
