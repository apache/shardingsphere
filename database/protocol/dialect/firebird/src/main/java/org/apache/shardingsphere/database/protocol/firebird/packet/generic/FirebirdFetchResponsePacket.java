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

package org.apache.shardingsphere.database.protocol.firebird.packet.generic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.binary.BinaryCell;
import org.apache.shardingsphere.database.protocol.binary.BinaryRow;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValueFactory;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.ISCConstants;

/**
 * SQL fetch packet for Firebird.
 */
@Getter
@RequiredArgsConstructor
public final class FirebirdFetchResponsePacket extends FirebirdPacket {
    
    private final int status;
    
    private final int count;
    
    private final BinaryRow row;
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        payload.writeInt4(FirebirdCommandPacketType.FETCH_RESPONSE.getValue());
        payload.writeInt4(status);
        payload.writeInt4(count);
        writeRowData(payload, row);
    }
    
    static void writeRowData(final FirebirdPacketPayload payload, final BinaryRow row) {
        if (row == null) {
            return;
        }
        int nullBitsStartIndex = payload.getByteBuf().writerIndex();
        int nullBits = (row.getCells().size() + 7) / 8;
        nullBits += (4 - nullBits) & 3;
        payload.getByteBuf().writeZero(nullBits);
        int i = 0;
        for (BinaryCell cell : row.getCells()) {
            if (null != cell.getData()) {
                FirebirdBinaryProtocolValue type = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(cell.getColumnType());
                type.write(payload, cell.getData());
            } else {
                int nullBitsIndex = nullBitsStartIndex + i / 8;
                byte nullBitsByte = payload.getByteBuf().getByte(nullBitsIndex);
                payload.getByteBuf().setByte(nullBitsIndex, nullBitsByte | (1 << i % 8));
            }
            i++;
        }
    }
    
    /**
     * Get fetch row response packet.
     *
     * @param row binary row
     * @return fetch response packet
     */
    public static FirebirdFetchResponsePacket getFetchRowPacket(final BinaryRow row) {
        return new FirebirdFetchResponsePacket(ISCConstants.FETCH_OK, 1, row);
    }
    
    /**
     * Get fetch no more rows response packet.
     *
     * @return fetch response packet
     */
    public static FirebirdFetchResponsePacket getFetchNoMoreRowsPacket() {
        return new FirebirdFetchResponsePacket(ISCConstants.FETCH_NO_MORE_ROWS, 0, null);
    }
    
    /**
     * Get fetch end response packet.
     *
     * @return fetch response packet
     */
    public static FirebirdFetchResponsePacket getFetchEndPacket() {
        return new FirebirdFetchResponsePacket(ISCConstants.FETCH_OK, 0, null);
    }
}
