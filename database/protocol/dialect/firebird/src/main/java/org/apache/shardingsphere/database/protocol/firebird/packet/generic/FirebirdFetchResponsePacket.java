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
public final class FirebirdFetchResponsePacket extends FirebirdPacket {
    
    private final int status;
    
    private final int count;
    
    private final BinaryRow data;
    
    public FirebirdFetchResponsePacket(final BinaryRow row) {
        status = ISCConstants.FETCH_OK;
        count = 1;
        data = row;
    }
    
    public FirebirdFetchResponsePacket() {
        status = ISCConstants.FETCH_NO_MORE_ROWS;
        count = 0;
        data = null;
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        payload.writeInt4(FirebirdCommandPacketType.FETCH_RESPONSE.getValue());
        payload.writeInt4(status);
        payload.writeInt4(count);
        writeData(payload, data);
    }
    
    static void writeData(final FirebirdPacketPayload payload, final BinaryRow data) {
        if (data == null) {
            return;
        }
        int nullBitsStartIndex = payload.getByteBuf().writerIndex();
        int nullBits = (data.getCells().size() + 7) / 8;
        nullBits += (4 - nullBits) & 3;
        payload.getByteBuf().writeZero(nullBits);
        int i = 0;
        for (BinaryCell cell : data.getCells()) {
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
}
