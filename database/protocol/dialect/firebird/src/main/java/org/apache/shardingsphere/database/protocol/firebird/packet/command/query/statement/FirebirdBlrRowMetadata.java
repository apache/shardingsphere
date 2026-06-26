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
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.firebirdsql.gds.BlrConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Firebird BLR row metadata.
 */
@Getter
public final class FirebirdBlrRowMetadata {
    
    private final ByteBuf blr;
    
    private final int length;
    
    private final List<FirebirdBinaryColumnType> columnTypes;
    
    private FirebirdBlrRowMetadata(final ByteBuf blr, final int length, final List<FirebirdBinaryColumnType> columnTypes) {
        this.blr = blr;
        this.length = length;
        this.columnTypes = columnTypes;
    }
    
    /**
     * Parse FirebirdBlrRowMetadata from BLR buffer.
     *
     * @param blrBuffer BLR buffer
     * @return row binary
     */
    public static FirebirdBlrRowMetadata parseBLR(final ByteBuf blrBuffer) {
        int length = blrBuffer.readableBytes();
        List<FirebirdBinaryColumnType> columnTypes = parse(blrBuffer);
        return new FirebirdBlrRowMetadata(blrBuffer, length, columnTypes);
    }
    
    private static List<FirebirdBinaryColumnType> parse(final ByteBuf blrBuffer) {
        ByteBuf buffer = blrBuffer.duplicate();
        if (!buffer.isReadable()) {
            return new ArrayList<>(0);
        }
        buffer.skipBytes(4);
        int length = buffer.readUnsignedByte();
        length += 256 * buffer.readUnsignedByte();
        List<FirebirdBinaryColumnType> result = new ArrayList<>(length / 2);
        int blrType = buffer.readUnsignedByte();
        while (blrType != BlrConstants.blr_end) {
            FirebirdBinaryColumnType type = FirebirdBinaryColumnType.valueOfBLRType(blrType);
            result.add(type);
            buffer.skipBytes(getSkipCount(type) + 2);
            blrType = buffer.readUnsignedByte();
        }
        return result;
    }
    
    private static int getSkipCount(final FirebirdBinaryColumnType type) {
        switch (type) {
            case VARYING:
            case TEXT:
                return 4;
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
