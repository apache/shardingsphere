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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.constant.buffer.FirebirdParameterBuffer;
import org.apache.shardingsphere.database.protocol.firebird.constant.buffer.type.FirebirdTransactionParameterBufferType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;

/**
 * Firebird start transaction packet.
 */
@HighFrequencyInvocation
@Getter
public final class FirebirdStartTransactionPacket extends FirebirdCommandPacket {
    
    private final int handle;
    
    private final byte[] rawTpb;
    
    private final FirebirdParameterBuffer tpb = FirebirdTransactionParameterBufferType.createBuffer();
    
    @Getter(AccessLevel.NONE)
    private boolean parsed;
    
    public FirebirdStartTransactionPacket(final FirebirdPacketPayload payload) {
        payload.skipReserved(4);
        handle = payload.readInt4();
        ByteBuf buffer = payload.readBuffer();
        rawTpb = ByteBufUtil.getBytes(buffer);
    }
    
    /**
     * Get parsed transaction parameters.
     *
     * @return parsed parameters
     */
    FirebirdParameterBuffer getTpb() {
        if (!parsed) {
            if (0 != rawTpb.length) {
                ByteBuf buffer = Unpooled.wrappedBuffer(rawTpb);
                try {
                    tpb.parseBuffer(buffer);
                } finally {
                    buffer.release();
                }
            }
            parsed = true;
        }
        return tpb;
    }
    
    /**
     * Is auto commit.
     *
     * @return auto commit or not
     */
    public boolean isAutoCommit() {
        return null != getTpb().getValue(FirebirdTransactionParameterBufferType.AUTOCOMMIT);
    }
    
    /**
     * Is read only.
     *
     * @return read only or not
     */
    public boolean isReadOnly() {
        return null != getTpb().getValue(FirebirdTransactionParameterBufferType.READ);
    }
    
    /**
     * Get transaction isolation level based on the transaction parameter buffer.
     *
     * @return transaction isolation level
     */
    public TransactionIsolationLevel getIsolationLevel() {
        if (null != getTpb().getValue(FirebirdTransactionParameterBufferType.READ_COMMITTED)) {
            return TransactionIsolationLevel.READ_COMMITTED;
        }
        if (null != getTpb().getValue(FirebirdTransactionParameterBufferType.CONCURRENCY)) {
            return TransactionIsolationLevel.REPEATABLE_READ;
        }
        if (null != getTpb().getValue(FirebirdTransactionParameterBufferType.CONSISTENCY)) {
            return TransactionIsolationLevel.SERIALIZABLE;
        }
        return TransactionIsolationLevel.REPEATABLE_READ;
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
        return 8 + payload.getBufferLength(8);
    }
}
