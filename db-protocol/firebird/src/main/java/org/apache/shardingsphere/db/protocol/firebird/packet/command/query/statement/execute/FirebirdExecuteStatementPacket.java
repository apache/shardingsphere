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

package org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.execute;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.apache.shardingsphere.db.protocol.binary.BinaryCell;
import org.apache.shardingsphere.db.protocol.binary.BinaryRow;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValue;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValueFactory;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.BlrConstants;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.netty.buffer.Unpooled.buffer;

/**
 * Firebird allocate statement packet.
 */
@Getter
public final class FirebirdExecuteStatementPacket extends FirebirdCommandPacket {

    private final FirebirdCommandPacketType type;
    private final int statementId;
    private final int transactionId;
    private final List<FirebirdBinaryColumnType> parameterTypes;
    private final int message;
    private final FirebirdPacketPayload payload;

    public FirebirdExecuteStatementPacket(FirebirdPacketPayload payload) {
        type = FirebirdCommandPacketType.valueOf(payload.readInt4());
        statementId = payload.readInt4();
        transactionId = payload.readInt4();
        parameterTypes = parseBLR(payload.readBuffer());
        message = payload.readInt4();
        int msgCount = payload.readInt4();
        int length = (parameterTypes.size() + 7) / 8;
        payload.skipReserved(length);
        payload.skipPadding(length);
        this.payload = payload;
        //        while (msgCount-- != 0) {
        //            paramsValues.add(payload.readBuffer());
        //        }
        
    }
    
    private List<FirebirdBinaryColumnType> parseBLR(ByteBuf blrBuffer) {
        blrBuffer.skipBytes(4);
        List<FirebirdBinaryColumnType> result = new ArrayList<>(blrBuffer.readUnsignedByte() / 2);
        blrBuffer.skipBytes(1);
        while (blrBuffer.isReadable()) {
            int blrType = blrBuffer.readUnsignedByte();
            if (blrType == BlrConstants.blr_end) {
                break;
            }
            FirebirdBinaryColumnType type = FirebirdBinaryColumnType.valueOfBLRType(blrType);
            result.add(type);
            blrBuffer.skipBytes(getSkipCount(type) + 2);
        }
        return result;
    }
    
    private int getSkipCount(FirebirdBinaryColumnType type) {
        switch (type) {
            case VARYING:
            case TEXT:
                return 4;
            case NULL:
                return 2;
            case BLOB:
            case ARRAY:
            case LONG:
            case SHORT:
            case INT64:
            case QUAD:
            case INT128:
                return 1;
            default:
                return 0;
        }
    }

    /**
     * Read parameter values from packet.
     *
     * @param paramTypes parameter type of values
     * @param longDataIndexes indexes of long data
     * @return parameter values
     * @throws SQLException SQL exception
     */
    public List<Object> readParameters(final List<FirebirdBinaryColumnType> paramTypes, final Set<Integer> longDataIndexes) throws SQLException {
        List<Object> result = new ArrayList<>(paramTypes.size());
        for (int paramIndex = 0; paramIndex < paramTypes.size(); paramIndex++) {
            if (longDataIndexes.contains(paramIndex)) {
                result.add(null);
                continue;
            }
            FirebirdBinaryProtocolValue binaryProtocolValue = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(paramTypes.get(paramIndex));
            Object value = binaryProtocolValue.read(payload);
            result.add(value);
        }
        return result;
    }
    
    /**
     * Returns true if, and only if, operation is a stored procedure.
     *
     * @return Whether the operation is a stored procedure
     */
    public boolean isStoredProcedure() {
        return type == FirebirdCommandPacketType.EXECUTE2;
    }
    
    /**
     * Get return data
     *
     * @param row Returned row
     * @return Return data
     */
    public byte[] getReturnData(BinaryRow row) {
        List<FirebirdBinaryColumnType> returnColumns = parseBLR(payload.readBuffer());
        ByteBuf writeBuffer = buffer(payload.getByteBuf().capacity());
        FirebirdPacketPayload writePayload = new FirebirdPacketPayload(writeBuffer, payload.getCharset());
        int nullBits = (returnColumns.size() + 7) / 8;
        nullBits += (4 - nullBits) & 3;
        writePayload.getByteBuf().writeZero(nullBits);
        for (BinaryCell cell : row.getCells()) {
            boolean isRowFind = returnColumns.remove((FirebirdBinaryColumnType) cell.getColumnType());
            if (isRowFind) {
                FirebirdBinaryProtocolValue type = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(cell.getColumnType());
                type.write(writePayload, cell.getData());
            }
        }
        return writePayload.getByteBuf().capacity(writePayload.getByteBuf().writerIndex()).array();
    }

    @Override
    protected void write(final FirebirdPacketPayload payload) {}
}
