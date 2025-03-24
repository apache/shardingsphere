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

package org.apache.shardingsphere.db.protocol.firebird.packet.generic;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.apache.shardingsphere.db.protocol.binary.BinaryCell;
import org.apache.shardingsphere.db.protocol.binary.BinaryRow;
import org.apache.shardingsphere.db.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValue;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValueFactory;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.ISCConstants;

/**
 * SQL fetch packet for Firebird.
 */
@Getter
public final class FirebirdFetchPacket extends FirebirdPacket {

    private final int status;
    private final int count;
    private final ByteBuf data;
    
    public FirebirdFetchPacket(BinaryRow row, FirebirdPacketPayload payload) {
        status = ISCConstants.FETCH_OK;
        count = 1;
        ByteBuf writeBuffer = payload.getByteBuf().alloc().buffer();
        FirebirdPacketPayload writePayload = new FirebirdPacketPayload(writeBuffer, payload.getCharset());
        int nullBits = (row.getCells().size() + 7) / 8;
        nullBits += (4 - nullBits) & 3;
        writePayload.getByteBuf().writeZero(nullBits);
        int i = 0;
        for (BinaryCell cell : row.getCells()) {
            if (cell.getData() != null) {
                FirebirdBinaryProtocolValue type = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(cell.getColumnType());
                type.write(writePayload, cell.getData());
            }
            else {
                byte nullBitsByte = writePayload.getByteBuf().getByte(i / 8);
                writePayload.getByteBuf().setByte(i / 8, nullBitsByte | (1 << i % 8));
            }
            i++;
        }
        data = writeBuffer;
    }
    
    public FirebirdFetchPacket() {
        status = ISCConstants.FETCH_NO_MORE_ROWS;
        count = 0;
        data = null;
    }
    
    @Override
    protected void write(FirebirdPacketPayload payload) {
        payload.writeInt4(FirebirdCommandPacketType.FETCH_RESPONSE.getValue());
        payload.writeInt4(status);
        payload.writeInt4(count);
        if (data != null) {
            payload.getByteBuf().writeBytes(data);
        }
    }
}