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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.firebirdsql.gds.BlrConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Firebird BLR row metadata.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class FirebirdBlrRowMetadata {
    
    private final int length;
    
    private final List<FirebirdBinaryColumnType> columnTypes;
    
    private final List<Integer> columnCharsets;
    
    private final List<Integer> columnLengths;
    
    /**
     * Parse FirebirdBlrRowMetadata from BLR buffer.
     *
     * <p>Character set and length are kept for {@code blr_text2} and {@code blr_varying2} columns; other columns get {@code 0}.</p>
     *
     * @param blrBuffer BLR buffer
     * @return row binary
     */
    public static FirebirdBlrRowMetadata parseBLR(final ByteBuf blrBuffer) {
        ByteBuf buffer = blrBuffer.duplicate();
        if (!buffer.isReadable()) {
            return new FirebirdBlrRowMetadata(blrBuffer.readableBytes(), new ArrayList<>(0), new ArrayList<>(0), new ArrayList<>(0));
        }
        buffer.skipBytes(4);
        int length = buffer.readUnsignedByte();
        length += 256 * buffer.readUnsignedByte();
        List<FirebirdBinaryColumnType> columnTypes = new ArrayList<>(length / 2);
        List<Integer> columnCharsets = new ArrayList<>(length / 2);
        List<Integer> columnLengths = new ArrayList<>(length / 2);
        int blrType = buffer.readUnsignedByte();
        while (blrType != BlrConstants.blr_end) {
            FirebirdBinaryColumnType type = FirebirdBinaryColumnType.valueOfBLRType(blrType);
            columnTypes.add(type);
            if (FirebirdBinaryColumnType.VARYING == type || FirebirdBinaryColumnType.TEXT == type) {
                columnCharsets.add(buffer.readUnsignedShortLE());
                columnLengths.add(buffer.readUnsignedShortLE());
            } else {
                columnCharsets.add(0);
                columnLengths.add(0);
                buffer.skipBytes(getSkipCount(type));
            }
            buffer.skipBytes(2);
            blrType = buffer.readUnsignedByte();
        }
        return new FirebirdBlrRowMetadata(blrBuffer.readableBytes(), columnTypes, columnCharsets, columnLengths);
    }
    
    private static int getSkipCount(final FirebirdBinaryColumnType type) {
        switch (type) {
            case NULL:
            case LEGACY_TEXT:
            case LEGACY_VARYING:
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
}
