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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.firebirdsql.gds.BlrConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Firebird batch message format parser, port of {@code PARSE_msg_format} / {@code parse_format} from Firebird {@code parser.cpp}.
 */
@Getter
public final class FirebirdParseBatchBlr {
    
    private static final int HEADER_LENGTH = 4;
    
    private final List<FirebirdBatchColumnDescriptor> fields;
    
    private final int messageLength;
    
    private final int netLength;
    
    private FirebirdParseBatchBlr(final List<FirebirdBatchColumnDescriptor> fields, final int messageLength, final int netLength) {
        this.fields = fields;
        this.messageLength = messageLength;
        this.netLength = netLength;
    }
    
    /**
     * Parse a BLR message buffer into its message format.
     *
     * @param blr BLR buffer
     * @param blrLength BLR length
     * @return parsed message format
     * @throws IllegalArgumentException when BLR format is invalid
     */
    public static FirebirdParseBatchBlr parse(final ByteBuf blr, final int blrLength) {
        if (blrLength < HEADER_LENGTH) {
            throw new IllegalArgumentException("BLR is too short: " + blrLength);
        }
        ByteBuf buffer = blr.duplicate();
        int version = buffer.readUnsignedByte();
        if (BlrConstants.blr_version4 != version && BlrConstants.blr_version5 != version) {
            throw new IllegalArgumentException("Unsupported BLR version: " + version);
        }
        if (BlrConstants.blr_begin != buffer.readUnsignedByte()) {
            throw new IllegalArgumentException("Expected blr_begin");
        }
        if (BlrConstants.blr_message != buffer.readUnsignedByte()) {
            throw new IllegalArgumentException("Expected blr_message");
        }
        buffer.skipBytes(1);
        return parseFormat(buffer);
    }
    
    private static FirebirdParseBatchBlr parseFormat(final ByteBuf buffer) {
        int count = buffer.readUnsignedByte();
        count += buffer.readUnsignedByte() << 8;
        int columnCount = count / 2;
        List<FirebirdBatchColumnDescriptor> fields = new ArrayList<>(columnCount);
        int offset = 0;
        int netLength = 0;
        for (int i = 0; i < columnCount; i++) {
            int blrType = buffer.readUnsignedByte();
            FirebirdBatchColumnDescriptor descriptor = readDescriptor(buffer, blrType);
            offset = alignTo(offset, alignmentOf(blrType));
            final int fieldOffset = offset;
            offset += descriptor.getLength();
            netLength += isVarying(descriptor.getType())
                    ? HEADER_LENGTH + alignTo(descriptor.getLength() - Short.BYTES, 4)
                    : alignTo(descriptor.getLength(), 4);
            offset = appendNullIndicator(buffer, offset);
            fields.add(new FirebirdBatchColumnDescriptor(descriptor.getType(), descriptor.getLength(), descriptor.getScale(), fieldOffset));
        }
        return new FirebirdParseBatchBlr(fields, offset, netLength);
    }
    
    private static int appendNullIndicator(final ByteBuf buffer, final int offset) {
        int nullType = buffer.readUnsignedByte();
        buffer.skipBytes(1);
        if (BlrConstants.blr_short != nullType) {
            throw new IllegalArgumentException("Expected blr_short NULL indicator, got: " + nullType);
        }
        return alignTo(offset, Short.BYTES) + Short.BYTES;
    }
    
    private static boolean isVarying(final FirebirdBinaryColumnType type) {
        return FirebirdBinaryColumnType.VARYING == type || FirebirdBinaryColumnType.LEGACY_VARYING == type;
    }
    
    private static FirebirdBatchColumnDescriptor readDescriptor(final ByteBuf buffer, final int blrType) {
        if (BlrConstants.blr_blob2 == blrType) {
            // TODO Implement BATCH_REGBLOB, BATCH_BLOB_STREAM and BATCH_SET_BPB before accepting BLOB fields.
            throw new FirebirdProtocolException("BLOB fields are not supported in Firebird batch operations");
        }
        FirebirdBinaryColumnType type = FirebirdBinaryColumnType.valueOfBLRType(blrType);
        if (BlrConstants.blr_text == blrType || BlrConstants.blr_varying == blrType) {
            int length = buffer.readUnsignedByte();
            length += buffer.readUnsignedByte() << 8;
            return new FirebirdBatchColumnDescriptor(type, BlrConstants.blr_varying == blrType ? length + Short.BYTES : length, 0, 0);
        }
        if (BlrConstants.blr_text2 == blrType || BlrConstants.blr_varying2 == blrType) {
            int scale = buffer.readUnsignedByte();
            scale += buffer.readUnsignedByte() << 8;
            int length = buffer.readUnsignedByte();
            length += buffer.readUnsignedByte() << 8;
            return new FirebirdBatchColumnDescriptor(type, BlrConstants.blr_varying2 == blrType ? length + Short.BYTES : length, scale, 0);
        }
        if (BlrConstants.blr_short == blrType || BlrConstants.blr_long == blrType || BlrConstants.blr_int64 == blrType
                || BlrConstants.blr_quad == blrType || BlrConstants.blr_int128 == blrType) {
            int scale = buffer.readByte();
            return new FirebirdBatchColumnDescriptor(type, fixedLengthOf(blrType), scale, 0);
        }
        return new FirebirdBatchColumnDescriptor(type, fixedLengthOf(blrType), 0, 0);
    }
    
    private static int fixedLengthOf(final int blrType) {
        if (BlrConstants.blr_short == blrType) {
            return Short.BYTES;
        }
        if (BlrConstants.blr_long == blrType || BlrConstants.blr_float == blrType || BlrConstants.blr_sql_date == blrType || BlrConstants.blr_sql_time == blrType) {
            return Integer.BYTES;
        }
        if (BlrConstants.blr_int64 == blrType || BlrConstants.blr_double == blrType || BlrConstants.blr_d_float == blrType
                || BlrConstants.blr_quad == blrType || BlrConstants.blr_timestamp == blrType) {
            return Long.BYTES;
        }
        if (BlrConstants.blr_int128 == blrType) {
            return Long.BYTES * 2;
        }
        if (BlrConstants.blr_bool == blrType) {
            return Byte.BYTES;
        }
        throw new IllegalArgumentException("Unsupported BLR type: " + blrType);
    }
    
    private static int alignmentOf(final int blrType) {
        if (BlrConstants.blr_text == blrType || BlrConstants.blr_text2 == blrType || BlrConstants.blr_bool == blrType) {
            return 1;
        }
        if (BlrConstants.blr_varying == blrType || BlrConstants.blr_varying2 == blrType || BlrConstants.blr_short == blrType) {
            return Short.BYTES;
        }
        if (BlrConstants.blr_int64 == blrType || BlrConstants.blr_double == blrType || BlrConstants.blr_d_float == blrType) {
            return Long.BYTES;
        }
        return Integer.BYTES;
    }
    
    private static int alignTo(final int value, final int alignment) {
        return value + alignment - 1 & ~(alignment - 1);
    }
    
}
