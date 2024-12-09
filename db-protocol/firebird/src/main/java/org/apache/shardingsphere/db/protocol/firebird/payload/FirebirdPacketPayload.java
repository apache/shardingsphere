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

package org.apache.shardingsphere.db.protocol.firebird.payload;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;

import java.nio.charset.Charset;

/**
 * Payload operation for Firebird packet data types.
 *
 * @see <a href="https://firebirdsql.org/file/documentation/html/en/firebirddocs/wireprotocol/firebird-wire-protocol.html#wireprotocol-appendix-types">Data Types</a>
 */
@RequiredArgsConstructor
@Getter
public final class FirebirdPacketPayload implements PacketPayload {
    
    private final ByteBuf byteBuf;
    
    private final Charset charset;

    /**
     * Read 1 byte fixed length integer from byte buffers.
     *
     * @return 1 byte fixed length integer
     */
    public int readInt1() {
        return byteBuf.readUnsignedByte();
    }

    /**
     * Write 1 byte fixed length integer to byte buffers.
     *
     * @param value 1 byte fixed length integer
     */
    public void writeInt1(final int value) {
        byteBuf.writeByte(value);
    }

    /**
     * Read 2 byte fixed length integer from byte buffers.
     *
     * @return 2 byte fixed length integer
     */
    public int readInt2() {
        return byteBuf.readUnsignedShort();
    }

    /**
     * Write 2 byte fixed length integer to byte buffers.
     *
     * @param value 2 byte fixed length integer
     */
    public void writeInt2(final int value) {
        byteBuf.writeShort(value);
    }

    /**
     * Read 2 byte fixed length integer in little-endian from byte buffers.
     *
     * @return 2 byte fixed length integer
     */
    public int readInt2LE() {
        return byteBuf.readUnsignedShortLE();
    }

    /**
     * Write 2 byte fixed length integer in little-endian to byte buffers.
     *
     * @param value 2 byte fixed length integer
     */
    public void writeInt2LE(final int value) {
        byteBuf.writeShortLE(value);
    }

    /**
     * Read 4 byte fixed length integer from byte buffers.
     *
     * @return 4 byte fixed length integer
     */
    public int readInt4() {
        return byteBuf.readInt();
    }
    
    /**
     * Write 4 byte fixed length integer to byte buffers.
     *
     * @param value 4 byte fixed length integer
     */
    public void writeInt4(final int value) {
        byteBuf.writeInt(value);
    }
    
    /**
     * Read 8 byte fixed length integer from byte buffers.
     *
     * @return 8 byte fixed length integer
     */
    public long readInt8() {
        return byteBuf.readLong();
    }
    
    /**
     * Write 8 byte fixed length integer to byte buffers.
     *
     * @param value 8 byte fixed length integer
     */
    public void writeInt8(final long value) {
        byteBuf.writeLong(value);
    }

    /**
     * Read specified length of bytes from byte buffers.
     *
     * @param count fixed number of bytes to read
     *
     * @return ByteBuf consisting of a specified number of bytes
     */
    public ByteBuf readBytes(final int count) {
        return byteBuf.readBytes(count);
    }

    /**
     * Write variable length bytes to byte buffers.
     * 
     * @param value fixed length bytes
     */
    public void writeBytes(final byte[] value) {
        byteBuf.writeBytes(value);
    }

    /**
     * Read variable length of bytes from byte buffers.
     *
     * @return ByteBuf consisting of a variable number of bytes
     */
    public ByteBuf readBuffer() {
        int length = byteBuf.readInt();
        ByteBuf buffer = byteBuf.readBytes(length);
        skipPadding(length);
        return buffer;
    }

    /**
     * Write variable length bytes to byte buffers.
     *
     * @param value fixed length bytes
     */
    public void writeBuffer(final byte[] value) {
        byteBuf.writeInt(value.length);
        byteBuf.writeBytes(value);
        byteBuf.writeBytes(new byte[(4 - value.length) & 3]);
    }

    /**
     * Read variable length of bytes from byte buffers.
     *
     * @return ByteBuf consisting of a variable number of bytes
     */
    public String readString() {
        return this.readBuffer().toString(charset);
    }

    /**
     * Write variable length bytes to byte buffers.
     *
     * @param value fixed length bytes
     */
    public void writeString(final String value) {
        this.writeBuffer(value.getBytes(charset));
    }
    
    /**
     * Bytes before zero.
     *
     * @return the number of bytes before zero
     */
    public int bytesBeforeZero() {
        return byteBuf.bytesBefore((byte) 0);
    }
    
    /**
     * Read null terminated string from byte buffers.
     * 
     * @return null terminated string
     */
    public String readStringNul() {
        String result = byteBuf.readCharSequence(byteBuf.bytesBefore((byte) 0), charset).toString();
        byteBuf.skipBytes(1);
        return result;
    }
    
    /**
     * Write null terminated string to byte buffers.
     * 
     * @param value null terminated string
     */
    public void writeStringNul(final String value) {
        byteBuf.writeBytes(value.getBytes(charset));
        byteBuf.writeByte(0);
    }
    
    /**
     * Write rest of packet string to byte buffers.
     * 
     * @param value rest of packet string
     */
    public void writeStringEOF(final String value) {
        byteBuf.writeBytes(value.getBytes(charset));
    }
    
    /**
     * Skip reserved from byte buffers.
     * 
     * @param length length of reserved
     */
    public void skipReserved(final int length) {
        byteBuf.skipBytes(length);
    }

    /**
     * Skip padding from byte buffers.
     *
     * @param length length of byte array that may have padding
     */
    public void skipPadding(final int length) {
        byteBuf.skipBytes((4 - length) & 3);
    }
    
    /**
     * Check if there has complete packet in ByteBuf.
     * PostgreSQL Message: (byte1) message type + (int4) length + (length - 4) payload
     *
     * @return has complete packet
     */
    public boolean hasCompletePacket() {
        return byteBuf.readableBytes() >= 5 && byteBuf.readableBytes() - 1 >= byteBuf.getInt(byteBuf.readerIndex() + 1);
    }
}
