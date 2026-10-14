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
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBpbVersionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidClumpletStructureException;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.firebirdsql.gds.ISCConstants;

import java.util.Arrays;

/**
 * BLOB stream state of a Firebird batch.
 *
 * <p>Mirrors {@code Rsr::BatchStream} of the Firebird remote protocol. {@code op_batch_blob_stream} declares the length of the
 * client stream buffer, but the alignment padding is not sent, a segment length is sent as 4 bytes instead of 2 and the tail of a
 * BLOB header split by the client buffer is sent as part of the next packet. Reading a packet therefore depends on the state left
 * by the previous packets of the same batch. Only creating the batch resets the state, and cancelling the batch keeps it.</p>
 *
 * <p>As Firebird does, the stream is read with unsigned 32-bit lengths and is rejected only when it cannot be decoded,
 * because Firebird checks the BLOBs restored from the stream when executing the batch.</p>
 */
@HighFrequencyInvocation
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public final class FirebirdBatchBlobStream {
    
    public static final int ALIGNMENT = 4;
    
    public static final int HEADER_LENGTH = 16;
    
    static final int SEGMENT_ALIGNMENT = 2;
    
    static final int SEGMENT_LENGTH_SIZE = 2;
    
    private static final int MAX_BPB_INTEGER_LENGTH = 4;
    
    private static final int WIRE_SEGMENT_LENGTH_SIZE = 4;
    
    private static final byte[] NO_BPB = new byte[0];
    
    private static final Handler SKIPPING_HANDLER = new Handler() {
        
        @Override
        public void onPadding(final int length) {
        }
        
        @Override
        public void onBlobHeader(final long batchBlobId, final long blobLength, final long bpbLength) {
        }
        
        @Override
        public void onSegment(final int segmentLength) {
        }
        
        @Override
        public void onBytes(final ByteBuf data) {
        }
    };
    
    private boolean defaultSegmented;
    
    private boolean segmented;
    
    private long blobRemaining;
    
    private long bpbRemaining;
    
    private int segmentRemaining;
    
    private byte[] bpb = NO_BPB;
    
    /**
     * Copy stream state.
     *
     * @return copied stream state
     */
    public FirebirdBatchBlobStream copy() {
        return new FirebirdBatchBlobStream(defaultSegmented, segmented, blobRemaining, bpbRemaining, segmentRemaining, bpb);
    }
    
    /**
     * Set default BLOB parameter buffer, which decides whether a BLOB without its own parameter buffer is segmented.
     *
     * <p>Mirrors {@code fb_utils::isBpbSegmented}, which reads the BLOB parameter buffer with {@code ClumpletReader}.</p>
     *
     * @param defaultBpb default BLOB parameter buffer
     * @throws InvalidClumpletStructureException when BLOB parameter buffer has invalid structure
     * @throws InvalidBpbVersionException when BLOB parameter buffer has wrong version
     */
    public void setDefaultBpb(final ByteBuf defaultBpb) {
        defaultSegmented = isSegmented(ByteBufUtil.getBytes(defaultBpb));
    }
    
    /**
     * Skip one stream portion of {@code op_batch_blob_stream}.
     *
     * <p>The tail of a BLOB header which the client sends with the next packet is not skipped.</p>
     *
     * @param data packet data starting at the stream portion
     * @param length declared length of the stream portion
     * @return whether the packet data holds the whole stream portion; if not, the stream state and the reader index of the data are undefined
     * @throws DatabaseProtocolException when stream portion is invalid
     * @throws InvalidClumpletStructureException when BLOB parameter buffer of stream portion has invalid structure
     * @throws InvalidBpbVersionException when BLOB parameter buffer of stream portion has wrong version
     */
    boolean skip(final ByteBuf data, final long length) {
        return read(data, length, SKIPPING_HANDLER);
    }
    
    /**
     * Read one stream portion of {@code op_batch_blob_stream} as Firebird restores it for its BLOB buffer.
     *
     * <p>BLOB headers and segment lengths are restored in little-endian byte order and alignment padding is restored as zero bytes.
     * The restored portion excludes the tail of a BLOB header which the client sends with the next packet.</p>
     *
     * @param data packet data starting at the stream portion
     * @param length declared length of the stream portion
     * @return restored stream portion
     * @throws IndexOutOfBoundsException when packet data is incomplete
     * @throws DatabaseProtocolException when stream portion is invalid
     * @throws InvalidClumpletStructureException when BLOB parameter buffer of stream portion has invalid structure
     * @throws InvalidBpbVersionException when BLOB parameter buffer of stream portion has wrong version
     */
    byte[] read(final ByteBuf data, final long length) {
        ByteBuf result = Unpooled.buffer((int) length);
        ShardingSpherePreconditions.checkState(read(data, length, new RestoringHandler(result)),
                () -> new IndexOutOfBoundsException(String.format("Batch BLOB stream portion of %d bytes is incomplete, only %d bytes are readable", length, data.readableBytes())));
        return ByteBufUtil.getBytes(result);
    }
    
    private boolean read(final ByteBuf data, final long length, final Handler handler) {
        ShardingSpherePreconditions.checkState(0L == length % ALIGNMENT, () -> new DatabaseProtocolException("Invalid batch BLOB stream length %d", length));
        long remains = length;
        while (remains > 0L) {
            long position = length - remains;
            if (0L == blobRemaining) {
                int padding = getPadding(position, ALIGNMENT);
                if (padding > 0) {
                    handler.onPadding(padding);
                    remains -= padding;
                    continue;
                }
                if (remains < HEADER_LENGTH) {
                    return true;
                }
                if (data.readableBytes() < HEADER_LENGTH) {
                    return false;
                }
                readHeader(data, handler);
                remains -= HEADER_LENGTH;
                continue;
            }
            if (bpbRemaining > 0L) {
                long bpbLength = Math.min(bpbRemaining, remains);
                if (data.readableBytes() < bpbLength) {
                    return false;
                }
                readBpb(data, (int) bpbLength, handler);
                remains -= bpbLength;
                continue;
            }
            if (segmented && 0 == segmentRemaining) {
                int padding = getPadding(position, SEGMENT_ALIGNMENT);
                if (padding > 0) {
                    handler.onPadding(padding);
                    remains -= padding;
                    blobRemaining -= padding;
                    continue;
                }
                if (data.readableBytes() < WIRE_SEGMENT_LENGTH_SIZE) {
                    return false;
                }
                readSegmentHeader(data, handler);
                remains -= SEGMENT_LENGTH_SIZE;
                continue;
            }
            long dataLength = Math.min(segmented ? segmentRemaining : blobRemaining, remains);
            if (data.readableBytes() < dataLength) {
                return false;
            }
            readData(data, (int) dataLength, handler);
            remains -= dataLength;
        }
        return true;
    }
    
    private void readHeader(final ByteBuf data, final Handler handler) {
        long batchBlobId = data.readLong();
        long blobLength = data.readUnsignedInt();
        long bpbLength = data.readUnsignedInt();
        blobRemaining = blobLength;
        bpbRemaining = bpbLength;
        segmentRemaining = 0;
        bpb = NO_BPB;
        if (0L == bpbLength) {
            segmented = defaultSegmented;
        }
        handler.onBlobHeader(batchBlobId, blobLength, bpbLength);
    }
    
    private void readBpb(final ByteBuf data, final int length, final Handler handler) {
        ByteBuf chunk = data.readSlice(length);
        int receivedLength = bpb.length;
        bpb = Arrays.copyOf(bpb, receivedLength + length);
        chunk.getBytes(chunk.readerIndex(), bpb, receivedLength, length);
        bpbRemaining -= length;
        blobRemaining = Integer.toUnsignedLong((int) (blobRemaining - length));
        if (0L == bpbRemaining) {
            segmented = isSegmented(bpb);
            bpb = NO_BPB;
        }
        handler.onBytes(chunk);
    }
    
    private void readSegmentHeader(final ByteBuf data, final Handler handler) {
        int segmentLength = data.readInt() & 0xFFFF;
        blobRemaining = Integer.toUnsignedLong((int) blobRemaining - SEGMENT_LENGTH_SIZE);
        ShardingSpherePreconditions.checkState(segmentLength <= blobRemaining,
                () -> new DatabaseProtocolException("Batch BLOB segment length %d exceeds BLOB length %d", segmentLength, blobRemaining));
        segmentRemaining = segmentLength;
        handler.onSegment(segmentLength);
    }
    
    private void readData(final ByteBuf data, final int length, final Handler handler) {
        handler.onBytes(data.readSlice(length));
        blobRemaining -= length;
        if (segmented) {
            segmentRemaining -= length;
        }
    }
    
    static int getPadding(final long position, final int alignment) {
        int remainder = (int) (position % alignment);
        return 0 == remainder ? 0 : alignment - remainder;
    }
    
    static boolean isSegmented(final byte[] bpb) {
        ShardingSpherePreconditions.checkState(bpb.length > 0, () -> new InvalidClumpletStructureException("empty buffer", 0));
        ShardingSpherePreconditions.checkState(ISCConstants.isc_bpb_version1 == bpb[0], () -> new InvalidBpbVersionException(bpb[0] & 0xFF, ISCConstants.isc_bpb_version1));
        int index = 1;
        while (index < bpb.length) {
            int valueLength = getClumpletValueLength(bpb, index);
            if (ISCConstants.isc_bpb_type == bpb[index]) {
                ShardingSpherePreconditions.checkState(valueLength <= MAX_BPB_INTEGER_LENGTH, () -> new InvalidClumpletStructureException("length of integer exceeds 4 bytes", valueLength));
                return 0 == valueLength || 0 == (bpb[index + 2] & ISCConstants.isc_bpb_type_stream);
            }
            index += 2 + valueLength;
        }
        return true;
    }
    
    private static int getClumpletValueLength(final byte[] bpb, final int index) {
        int availableLength = bpb.length - index;
        ShardingSpherePreconditions.checkState(availableLength >= 2, () -> new InvalidClumpletStructureException("buffer end before end of clumplet - no length component", availableLength));
        int result = bpb[index + 1] & 0xFF;
        ShardingSpherePreconditions.checkState(2 + result <= availableLength, () -> new InvalidClumpletStructureException("buffer end before end of clumplet - clumplet too long", 2 + result));
        return result;
    }
    
    private interface Handler {
        
        void onPadding(int length);
        
        void onBlobHeader(long batchBlobId, long blobLength, long bpbLength);
        
        void onSegment(int segmentLength);
        
        void onBytes(ByteBuf data);
    }
    
    @HighFrequencyInvocation
    @RequiredArgsConstructor
    private static final class RestoringHandler implements Handler {
        
        private final ByteBuf restored;
        
        @Override
        public void onPadding(final int length) {
            restored.writeZero(length);
        }
        
        @Override
        public void onBlobHeader(final long batchBlobId, final long blobLength, final long bpbLength) {
            restored.writeIntLE((int) (batchBlobId >>> 32)).writeIntLE((int) batchBlobId).writeIntLE((int) blobLength).writeIntLE((int) bpbLength);
        }
        
        @Override
        public void onSegment(final int segmentLength) {
            restored.writeShortLE(segmentLength);
        }
        
        @Override
        public void onBytes(final ByteBuf data) {
            restored.writeBytes(data, data.readerIndex(), data.readableBytes());
        }
    }
}
