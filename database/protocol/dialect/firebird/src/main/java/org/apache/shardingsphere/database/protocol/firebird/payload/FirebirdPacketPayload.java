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

package org.apache.shardingsphere.database.protocol.firebird.payload;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;

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
     * Read 1 byte fixed length integer from unsigned byte buffers.
     *
     * @return 1 byte fixed length integer
     */
    public int readInt1Unsigned() {
        return byteBuf.readUnsignedByte();
    }
    
    /**
     * Read 1 byte fixed length integer from byte buffers.
     *
     * @return 1 byte fixed length integer
     */
    public int readInt1() {
        return byteBuf.readByte();
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
     * Read 4 byte unsigned fixed length integer from byte buffers.
     *
     * @return 4 byte fixed length integer
     */
    public long readInt4Unsigned() {
        return byteBuf.readUnsignedInt();
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
     * Write 4 byte fixed length integer in little-endian to byte buffers.
     *
     * @param value 4 byte fixed length integer
     */
    public void writeInt4LE(final int value) {
        byteBuf.writeIntLE(value);
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
     * @return ByteBuf consisting of a specified number of bytes
     */
    public ByteBuf readBytes(final int count) {
        return byteBuf.readSlice(count);
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
        ByteBuf buffer = byteBuf.readSlice(length);
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
     * Write variable length bytes to byte buffers.
     *
     * @param buffer buffer containing data
     */
    public void writeBuffer(final ByteBuf buffer) {
        byteBuf.writeInt(buffer.writerIndex());
        byteBuf.writeBytes(buffer);
        byteBuf.writeBytes(new byte[(4 - buffer.writerIndex()) & 3]);
    }
    
    /**
     * Read variable length of bytes from byte buffers.
     *
     * @return ByteBuf consisting of a variable number of bytes
     */
    public String readString() {
        return readBuffer().toString(charset);
    }
    
    /**
     * Write variable length bytes to byte buffers.
     *
     * @param value fixed length bytes
     */
    public void writeString(final String value) {
        writeBuffer(value.getBytes(charset));
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
        byteBuf.skipBytes(getPadding(length));
    }
    
    /**
     * Get padding from byte buffers.
     *
     * @param length length of byte array that may have padding
     * @return padding size needed to align to 4-byte boundary
     */
    public int getPadding(final int length) {
        return (4 - length) & 3;
    }
    
    /**
     * Get buffer length including padding.
     *
     * @param index index of buffer length
     * @return buffer length with padding added
     */
    public int getBufferLength(final int index) {
        int length = byteBuf.getInt(index) + 4;
        return length + getPadding(length);
    }
}
