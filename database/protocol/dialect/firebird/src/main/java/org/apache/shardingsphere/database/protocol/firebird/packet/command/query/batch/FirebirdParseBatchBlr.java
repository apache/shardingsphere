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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.firebirdsql.gds.BlrConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Firebird batch message format parser, port of {@code PARSE_msg_format} / {@code parse_format} from Firebird {@code parser.cpp}.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class FirebirdParseBatchBlr {
    
    private static final int HEADER_LENGTH = 4;
    
    private final List<FirebirdBatchColumnDescriptor> fields;
    
    private final int messageLength;
    
    private final int netLength;
    
    /**
     * Parse a BLR message buffer into its message format and validate that every field is supported.
     *
     * @param blr BLR buffer
     * @param blrLength BLR length
     * @return parsed message format
     * @throws IllegalArgumentException when BLR format is structurally invalid
     * @throws DatabaseProtocolException when BLR contains a field type that batch operations do not support yet
     */
    public static FirebirdParseBatchBlr parse(final ByteBuf blr, final int blrLength) {
        FirebirdParseBatchBlr result = parseForFraming(blr, blrLength);
        validateSupported(result.fields);
        return result;
    }
    
    /**
     * Parse a BLR message buffer into its message format for codec framing only, without semantic support validation.
     *
     * <p>Used by the packet codec to split a coalesced {@code BATCH_CREATE + BATCH_MSG} frame. Semantically unsupported
     * but structurally valid fields (such as BLOB) are returned as descriptors instead of being rejected here, so that the
     * rejection happens later on the command/error path rather than as a codec-level channel close.</p>
     *
     * @param blr BLR buffer
     * @param blrLength BLR length
     * @return parsed message format
     * @throws IllegalArgumentException when BLR format is structurally invalid
     */
    public static FirebirdParseBatchBlr parseForFraming(final ByteBuf blr, final int blrLength) {
        ShardingSpherePreconditions.checkState(blrLength >= HEADER_LENGTH, () -> new IllegalArgumentException("BLR is too short: " + blrLength));
        ByteBuf buffer = blr.duplicate();
        int startReaderIndex = buffer.readerIndex();
        int version = buffer.readUnsignedByte();
        ShardingSpherePreconditions.checkState(BlrConstants.blr_version4 == version || BlrConstants.blr_version5 == version, () -> new IllegalArgumentException("Unsupported BLR version: " + version));
        ShardingSpherePreconditions.checkState(BlrConstants.blr_begin == buffer.readUnsignedByte(), () -> new IllegalArgumentException("Expected blr_begin"));
        ShardingSpherePreconditions.checkState(BlrConstants.blr_message == buffer.readUnsignedByte(), () -> new IllegalArgumentException("Expected blr_message"));
        buffer.skipBytes(1);
        FirebirdParseBatchBlr result = parseFormat(buffer);
        validateTermination(buffer, startReaderIndex, blrLength);
        return result;
    }
    
    private static void validateTermination(final ByteBuf buffer, final int startReaderIndex, final int blrLength) {
        if (remainingWithinBlr(buffer, startReaderIndex, blrLength) < 1 || BlrConstants.blr_end != buffer.readUnsignedByte()) {
            throw new IllegalArgumentException("Expected blr_end");
        }
        if (remainingWithinBlr(buffer, startReaderIndex, blrLength) < 1 || BlrConstants.blr_eoc != buffer.readUnsignedByte()) {
            throw new IllegalArgumentException("Expected blr_eoc");
        }
        ShardingSpherePreconditions.checkState(0 == remainingWithinBlr(buffer, startReaderIndex, blrLength), () -> new IllegalArgumentException("Unexpected trailing bytes in BLR"));
    }
    
    private static int remainingWithinBlr(final ByteBuf buffer, final int startReaderIndex, final int blrLength) {
        return blrLength - (buffer.readerIndex() - startReaderIndex);
    }
    
    private static void validateSupported(final List<FirebirdBatchColumnDescriptor> fields) {
        for (FirebirdBatchColumnDescriptor each : fields) {
            // TODO Implement BATCH_REGBLOB, BATCH_BLOB_STREAM and BATCH_SET_BPB before accepting BLOB fields.
            ShardingSpherePreconditions.checkState(FirebirdBinaryColumnType.BLOB != each.getType(), () -> new DatabaseProtocolException("BLOB fields are not supported in Firebird batch operations"));
        }
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
            offset = alignTo(offset, alignmentOf(blrType));
            FirebirdBatchColumnDescriptor descriptor = readDescriptor(buffer, blrType, offset);
            offset += descriptor.getLength();
            netLength += isVarying(descriptor.getType()) ? HEADER_LENGTH + alignTo(descriptor.getLength() - Short.BYTES, 4) : alignTo(descriptor.getLength(), 4);
            offset = appendNullIndicator(buffer, offset);
            fields.add(descriptor);
        }
        return new FirebirdParseBatchBlr(fields, offset, netLength);
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
    
    private static FirebirdBatchColumnDescriptor readDescriptor(final ByteBuf buffer, final int blrType, final int offset) {
        if (BlrConstants.blr_blob2 == blrType) {
            buffer.skipBytes(4);
            return new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.BLOB, Long.BYTES, 0, offset);
        }
        if (BlrConstants.blr_quad == blrType) {
            int scale = buffer.readByte();
            return new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.BLOB, Long.BYTES, scale, offset);
        }
        FirebirdBinaryColumnType type = FirebirdBinaryColumnType.valueOfBLRType(blrType);
        if (BlrConstants.blr_text == blrType || BlrConstants.blr_varying == blrType) {
            int length = buffer.readUnsignedByte();
            length += buffer.readUnsignedByte() << 8;
            return new FirebirdBatchColumnDescriptor(type, BlrConstants.blr_varying == blrType ? length + Short.BYTES : length, 0, offset);
        }
        if (BlrConstants.blr_text2 == blrType || BlrConstants.blr_varying2 == blrType) {
            int scale = buffer.readUnsignedByte();
            scale += buffer.readUnsignedByte() << 8;
            int length = buffer.readUnsignedByte();
            length += buffer.readUnsignedByte() << 8;
            return new FirebirdBatchColumnDescriptor(type, BlrConstants.blr_varying2 == blrType ? length + Short.BYTES : length, scale, offset);
        }
        if (BlrConstants.blr_short == blrType || BlrConstants.blr_long == blrType || BlrConstants.blr_int64 == blrType || BlrConstants.blr_int128 == blrType) {
            int scale = buffer.readByte();
            return new FirebirdBatchColumnDescriptor(type, fixedLengthOf(blrType), scale, offset);
        }
        return new FirebirdBatchColumnDescriptor(type, fixedLengthOf(blrType), 0, offset);
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
    
    private static boolean isVarying(final FirebirdBinaryColumnType type) {
        return FirebirdBinaryColumnType.VARYING == type || FirebirdBinaryColumnType.LEGACY_VARYING == type;
    }
    
    private static int appendNullIndicator(final ByteBuf buffer, final int offset) {
        int nullType = buffer.readUnsignedByte();
        buffer.skipBytes(1);
        ShardingSpherePreconditions.checkState(BlrConstants.blr_short == nullType, () -> new IllegalArgumentException("Expected blr_short NULL indicator, got: " + nullType));
        return alignTo(offset, Short.BYTES) + Short.BYTES;
    }
    
    private static int alignTo(final int value, final int alignment) {
        return value + alignment - 1 & -alignment;
    }
}
