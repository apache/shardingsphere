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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.arrays;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.firebirdsql.gds.BlrConstants;

/**
 * Firebird put slice command packet.
 */
public final class FirebirdPutSliceCommandPacket extends FirebirdCommandPacket {
    
    private static final int SDL_VERSION_1 = 1;
    
    private static final int SDL_RELATION = 2;
    
    private static final int SDL_RELATION_ID = 3;
    
    private static final int SDL_FIELD = 4;
    
    private static final int SDL_FIELD_ID = 5;
    
    private static final int SDL_STRUCT = 6;
    
    private final int transactionHandle;
    
    private final long arrayHandle;
    
    private final int sliceLength;
    
    private final ByteBuf sliceDescriptor;
    
    private final ByteBuf sliceParameters;
    
    private final ByteBuf sliceData;
    
    public FirebirdPutSliceCommandPacket(final FirebirdPacketPayload payload) {
        payload.skipReserved(4);
        transactionHandle = payload.readInt4();
        arrayHandle = payload.readInt8();
        sliceLength = payload.readInt4();
        sliceDescriptor = payload.readBuffer();
        sliceParameters = payload.readBuffer();
        int dataLength = getSliceDataLength(sliceDescriptor, payload.getByteBuf().duplicate());
        payload.skipReserved(Integer.BYTES);
        sliceData = payload.readBytes(dataLength);
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
    }
    
    /**
     * Get the request length, including the opcode and slice data.
     *
     * @param payload Firebird packet payload
     * @return request length in bytes
     */
    public static int getLength(final FirebirdPacketPayload payload) {
        ByteBuf buffer = payload.getByteBuf().duplicate();
        int startIndex = buffer.readerIndex();
        FirebirdPacketPayload copy = new FirebirdPacketPayload(buffer, payload.getCharset());
        copy.skipReserved(4 + 4 + 8 + 4);
        ByteBuf descriptor = copy.readBuffer();
        copy.readBuffer();
        int dataLength = getSliceDataLength(descriptor, buffer.duplicate());
        return Math.addExact(buffer.readerIndex() - startIndex + Integer.BYTES, dataLength);
    }
    
    private static int getSliceDataLength(final ByteBuf descriptor, final ByteBuf data) {
        int nativeLength = data.readInt();
        ShardingSpherePreconditions.checkState(nativeLength >= 0, () -> new IllegalArgumentException("Negative slice length"));
        if (0 == nativeLength) {
            return 0;
        }
        ByteBuf sdl = descriptor.duplicate();
        ShardingSpherePreconditions.checkState(SDL_VERSION_1 == sdl.readUnsignedByte(), () -> new IllegalArgumentException("Invalid SDL version"));
        while (sdl.isReadable()) {
            int operation = sdl.readUnsignedByte();
            switch (operation) {
                case SDL_RELATION:
                case SDL_FIELD:
                    sdl.skipBytes(sdl.readUnsignedByte());
                    break;
                case SDL_RELATION_ID:
                case SDL_FIELD_ID:
                    sdl.skipBytes(2);
                    break;
                case SDL_STRUCT:
                    ShardingSpherePreconditions.checkState(1 == sdl.readUnsignedByte(), () -> new IllegalArgumentException("Invalid SDL element count"));
                    return getEncodedDataLength(sdl, data, nativeLength);
                default:
                    throw new IllegalArgumentException("Missing SDL element descriptor");
            }
        }
        throw new IllegalArgumentException("Missing SDL element descriptor");
    }
    
    private static int getEncodedDataLength(final ByteBuf sdl, final ByteBuf data, final int nativeLength) {
        int type = sdl.readUnsignedByte();
        int elementLength;
        int wireLength;
        if (BlrConstants.blr_text2 == type || BlrConstants.blr_varying2 == type || BlrConstants.blr_cstring2 == type) {
            sdl.skipBytes(2);
        }
        switch (type) {
            case BlrConstants.blr_text2:
            case BlrConstants.blr_varying2:
            case BlrConstants.blr_cstring2:
            case BlrConstants.blr_text:
            case BlrConstants.blr_varying:
            case BlrConstants.blr_cstring:
                int declaredLength = sdl.readUnsignedShortLE();
                elementLength = declaredLength + (BlrConstants.blr_varying == type || BlrConstants.blr_varying2 == type ? 2 : 0);
                ShardingSpherePreconditions.checkState(declaredLength > 0 && elementLength <= 65535, () -> new IllegalArgumentException("Invalid SDL text length"));
                if (BlrConstants.blr_text == type || BlrConstants.blr_text2 == type) {
                    wireLength = (elementLength + 3) & ~3;
                    break;
                }
                return getEncodedStringLength(data, nativeLength / elementLength, elementLength);
            case BlrConstants.blr_short:
                elementLength = 2;
                wireLength = 4;
                break;
            case BlrConstants.blr_bool:
                elementLength = 1;
                wireLength = 4;
                break;
            case BlrConstants.blr_long:
            case BlrConstants.blr_float:
            case BlrConstants.blr_sql_date:
            case BlrConstants.blr_sql_time:
                elementLength = 4;
                wireLength = 4;
                break;
            case BlrConstants.blr_int64:
            case BlrConstants.blr_quad:
            case BlrConstants.blr_double:
            case BlrConstants.blr_d_float:
            case BlrConstants.blr_dec64:
            case BlrConstants.blr_timestamp:
            case BlrConstants.blr_sql_time_tz:
                elementLength = 8;
                wireLength = 8;
                break;
            case BlrConstants.blr_ex_time_tz:
                elementLength = 8;
                wireLength = 12;
                break;
            case BlrConstants.blr_timestamp_tz:
                elementLength = 12;
                wireLength = 12;
                break;
            case BlrConstants.blr_ex_timestamp_tz:
                elementLength = 12;
                wireLength = 16;
                break;
            case BlrConstants.blr_int128:
            case BlrConstants.blr_dec128:
                elementLength = 16;
                wireLength = 16;
                break;
            default:
                throw new IllegalArgumentException("Unsupported SDL element type: " + type);
        }
        return Math.multiplyExact(nativeLength / elementLength, wireLength);
    }
    
    private static int getEncodedStringLength(final ByteBuf data, final int count, final int elementLength) {
        int startIndex = data.readerIndex();
        for (int i = 0; i < count; i++) {
            int length = Math.min(data.readInt() & 0xFFFF, elementLength - 1);
            data.skipBytes((length + 3) & ~3);
        }
        return data.readerIndex() - startIndex;
    }
}
