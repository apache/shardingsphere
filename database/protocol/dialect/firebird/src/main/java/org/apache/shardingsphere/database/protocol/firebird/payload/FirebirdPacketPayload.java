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
import lombok.Setter;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;

import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * Operations on Firebird protocol packet data.
 *
 * <p>The {@code LE} suffix means little-endian byte order: the least significant byte is written or read first.</p>
 *
 * @see <a href="https://firebirdsql.org/file/documentation/html/en/firebirddocs/wireprotocol/firebird-wire-protocol.html#wireprotocol-appendix-types">Data Types</a>
 */
@RequiredArgsConstructor
@Getter
public final class FirebirdPacketPayload implements PacketPayload {
    
    private final ByteBuf byteBuf;
    
    private final Charset charset;
    
    @Setter
    private int connectionId = -1;
    
    /**
     * Read a 1-byte unsigned integer from the buffer.
     *
     * <p>Reads the 8-bit representation of Firebird {@code UCHAR} and returns a Java {@code int} value.</p>
     *
     * @return unsigned integer read from 1 byte
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L57">Firebird UCHAR</a>
     */
    public int readInt1Unsigned() {
        return byteBuf.readUnsignedByte();
    }
    
    /**
     * Read a 1-byte signed integer from the buffer.
     *
     * <p>Reads the 8-bit representation of Firebird {@code SCHAR} and returns a Java {@code int} value.
     * Firebird defines {@code SCHAR} as C++ {@code char}, whose signedness is platform-dependent.</p>
     *
     * @return signed integer read from 1 byte
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L55">Firebird SCHAR</a>
     */
    public int readInt1() {
        return byteBuf.readByte();
    }
    
    /**
     * Write a 1-byte integer to the buffer.
     *
     * <p>Writes the low 8 bits of a Firebird {@code SCHAR} or {@code UCHAR} value.</p>
     *
     * @param value integer whose low 8 bits are written to the buffer
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L55-L57">Firebird SCHAR and UCHAR</a>
     */
    public void writeInt1(final int value) {
        byteBuf.writeByte(value);
    }
    
    /**
     * Read a 2-byte unsigned integer from the buffer.
     *
     * <p>Reads the 16-bit representation of Firebird {@code USHORT} in big-endian order and returns a Java {@code int} value.</p>
     *
     * @return unsigned integer read from 2 bytes
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L59">Firebird USHORT</a>
     */
    public int readInt2() {
        return byteBuf.readUnsignedShort();
    }
    
    /**
     * Read a 2-byte signed integer from the buffer.
     *
     * <p>Reads the 16-bit representation of Firebird {@code SSHORT} in big-endian order and returns a Java {@code int} value.</p>
     *
     * @return signed integer read from 2 bytes
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L58">Firebird SSHORT</a>
     */
    public int readInt2Signed() {
        return byteBuf.readShort();
    }
    
    /**
     * Write a 2-byte integer to the buffer.
     *
     * <p>Writes the low 16 bits of a Firebird {@code SSHORT} or {@code USHORT} value in big-endian order.</p>
     *
     * @param value integer whose low 16 bits are written to the buffer
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L58-L59">Firebird SSHORT and USHORT</a>
     */
    public void writeInt2(final int value) {
        byteBuf.writeShort(value);
    }
    
    /**
     * Read a 2-byte unsigned integer from the buffer.
     *
     * <p>Reads the 16-bit representation of Firebird {@code USHORT} in little-endian order and returns a Java {@code int} value.</p>
     *
     * @return unsigned integer read from 2 bytes
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L59">Firebird USHORT</a>
     */
    public int readInt2LE() {
        return byteBuf.readUnsignedShortLE();
    }
    
    /**
     * Read a 2-byte signed integer from the buffer.
     *
     * <p>Reads the 16-bit representation of Firebird {@code SSHORT} in little-endian order and returns a Java {@code int} value.</p>
     *
     * @return signed integer read from 2 bytes
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L58">Firebird SSHORT</a>
     */
    public int readInt2SignedLE() {
        return byteBuf.readShortLE();
    }
    
    /**
     * Write a 2-byte integer to the buffer.
     *
     * <p>Writes the low 16 bits of a Firebird {@code SSHORT} or {@code USHORT} value in little-endian order.</p>
     *
     * @param value integer whose low 16 bits are written to the buffer
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L58-L59">Firebird SSHORT and USHORT</a>
     */
    public void writeInt2LE(final int value) {
        byteBuf.writeShortLE(value);
    }
    
    /**
     * Read a 4-byte signed integer from the buffer.
     *
     * <p>Reads the 32-bit representation of Firebird {@code SLONG} in big-endian order and returns a Java {@code int} value.</p>
     *
     * @return signed integer read from 4 bytes
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L38-L49">Firebird SLONG</a>
     */
    public int readInt4() {
        return byteBuf.readInt();
    }
    
    /**
     * Read a 4-byte signed integer from the buffer.
     *
     * <p>Reads the 32-bit representation of Firebird {@code SLONG} in little-endian order and returns a Java {@code int} value.</p>
     *
     * @return signed integer read from 4 bytes
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L38-L49">Firebird SLONG</a>
     */
    public int readInt4LE() {
        return byteBuf.readIntLE();
    }
    
    /**
     * Read a BLOB handle from the buffer.
     *
     * <p>A BLOB handle may arrive as {@code 0xFFFF} or sign-extended as {@code 0xFFFFFFFF}, representing {@code INVALID_OBJECT}.
     * Masking the low 16 bits normalizes the value to the range 0 to 65535.
     * A Firebird {@code OBJCT} handle is transmitted as 4 bytes in big-endian order.</p>
     *
     * @return BLOB handle in the range 0 to 65535
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/remote/protocol.h#L160">Firebird OBJCT (USHORT)</a>
     */
    public int readBlobHandle() {
        return byteBuf.readInt() & 0xFFFF;
    }
    
    /**
     * Read a 4-byte unsigned integer from the buffer.
     *
     * <p>Reads the 32-bit representation of Firebird {@code ULONG} in big-endian order and returns a Java {@code long} value.</p>
     *
     * @return unsigned integer read from 4 bytes
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L38-L49">Firebird ULONG</a>
     */
    public long readInt4Unsigned() {
        return byteBuf.readUnsignedInt();
    }
    
    /**
     * Read a 4-byte unsigned integer from the buffer.
     *
     * <p>Reads the 32-bit representation of Firebird {@code ULONG} in little-endian order and returns a Java {@code long} value.</p>
     *
     * @return unsigned integer read from 4 bytes
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L38-L49">Firebird ULONG</a>
     */
    public long readInt4UnsignedLE() {
        return byteBuf.readUnsignedIntLE();
    }
    
    /**
     * Write a 4-byte integer to the buffer.
     *
     * <p>Writes the low 32 bits of a Firebird {@code SLONG} or {@code ULONG} value in big-endian order.</p>
     *
     * @param value integer whose low 32 bits are written to the buffer
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L38-L49">Firebird SLONG and ULONG</a>
     */
    public void writeInt4(final int value) {
        byteBuf.writeInt(value);
    }
    
    /**
     * Write a 4-byte integer to the buffer.
     *
     * <p>Writes the low 32 bits of a Firebird {@code SLONG} or {@code ULONG} value in little-endian order.</p>
     *
     * @param value integer whose low 32 bits are written to the buffer
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L38-L49">Firebird SLONG and ULONG</a>
     */
    public void writeInt4LE(final int value) {
        byteBuf.writeIntLE(value);
    }
    
    /**
     * Read a 8-byte signed integer from the buffer.
     *
     * <p>Reads the 64-bit representation of Firebird {@code SINT64} in big-endian order and returns a Java {@code long} value.</p>
     *
     * @return signed integer read from 8 bytes
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L61-L67">Firebird SINT64</a>
     */
    public long readInt8() {
        return byteBuf.readLong();
    }
    
    /**
     * Read a 8-byte signed integer from the buffer.
     *
     * <p>Reads the 64-bit representation of Firebird {@code SINT64} in little-endian order and returns a Java {@code long} value.</p>
     *
     * @return signed integer read from 8 bytes
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L61-L67">Firebird SINT64</a>
     */
    public long readInt8LE() {
        return byteBuf.readLongLE();
    }
    
    /**
     * Read a 8-byte unsigned integer from the buffer.
     *
     * <p>Reads the 64-bit representation of Firebird {@code FB_UINT64} in big-endian order and returns a Java {@link BigInteger} value.</p>
     *
     * @return integer in the range 0 to 18446744073709551615
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L61-L67">Firebird FB_UINT64</a>
     */
    public BigInteger readInt8Unsigned() {
        long value = byteBuf.readLong();
        return value >= 0L ? BigInteger.valueOf(value) : BigInteger.valueOf(value & Long.MAX_VALUE).setBit(Long.SIZE - 1);
    }
    
    /**
     * Read a 8-byte unsigned integer from the buffer.
     *
     * <p>Reads the 64-bit representation of Firebird {@code FB_UINT64} in little-endian order and returns a Java {@link BigInteger} value.</p>
     *
     * @return integer in the range 0 to 18446744073709551615
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L61-L67">Firebird FB_UINT64</a>
     */
    public BigInteger readInt8UnsignedLE() {
        long value = byteBuf.readLongLE();
        return value >= 0L ? BigInteger.valueOf(value) : BigInteger.valueOf(value & Long.MAX_VALUE).setBit(Long.SIZE - 1);
    }
    
    /**
     * Write a 8-byte integer to the buffer.
     *
     * <p>Writes the low 64 bits of a Firebird {@code SINT64} or {@code FB_UINT64} value in big-endian order.</p>
     *
     * @param value integer whose low 64 bits are written to the buffer
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L61-L67">Firebird SINT64 and FB_UINT64</a>
     */
    public void writeInt8(final long value) {
        byteBuf.writeLong(value);
    }
    
    /**
     * Write a 8-byte integer to the buffer.
     *
     * <p>Writes the low 64 bits of a Firebird {@code SINT64} or {@code FB_UINT64} value in little-endian order.</p>
     *
     * @param value integer whose low 64 bits are written to the buffer
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L61-L67">Firebird SINT64 and FB_UINT64</a>
     */
    public void writeInt8LE(final long value) {
        byteBuf.writeLongLE(value);
    }
    
    /**
     * Read the specified number of bytes from the buffer.
     *
     * <p>Reads raw Firebird {@code UCHAR} bytes without a length prefix or padding.</p>
     *
     * @param count number of bytes to read
     * @return {@link ByteBuf} containing the specified number of bytes
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L57">Firebird UCHAR</a>
     */
    public ByteBuf readBytes(final int count) {
        return byteBuf.readSlice(count);
    }
    
    /**
     * Write a byte array to the buffer.
     *
     * <p>Writes raw Firebird {@code UCHAR} bytes without a length prefix or padding.</p>
     *
     * @param value byte array to write
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L57">Firebird UCHAR</a>
     */
    public void writeBytes(final byte[] value) {
        byteBuf.writeBytes(value);
    }
    
    /**
     * Read variable-length data from the buffer.
     *
     * <p>Reads the Firebird {@code CSTRING} wire representation: a 32-bit big-endian length, data bytes and padding to a multiple of 4 bytes.</p>
     *
     * @return {@link ByteBuf} containing the data without the length prefix or padding
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/remote/protocol.cpp#L1395-L1450">Firebird CSTRING encoding</a>
     */
    public ByteBuf readBuffer() {
        int length = byteBuf.readInt();
        ByteBuf buffer = byteBuf.readSlice(length);
        skipPadding(length);
        return buffer;
    }
    
    /**
     * Write variable-length data to the buffer.
     *
     * <p>Writes the Firebird {@code CSTRING} wire representation: a 32-bit big-endian length, data bytes and padding to a multiple of 4 bytes.</p>
     *
     * @param value byte array to write
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/remote/protocol.cpp#L1395-L1450">Firebird CSTRING encoding</a>
     */
    public void writeBuffer(final byte[] value) {
        byteBuf.writeInt(value.length);
        byteBuf.writeBytes(value);
        writePadding(value.length);
    }
    
    /**
     * Write variable-length data to the buffer.
     *
     * <p>Writes the Firebird {@code CSTRING} wire representation: a 32-bit big-endian length, data bytes and padding to a multiple of 4 bytes.</p>
     *
     * @param buffer buffer containing the data to write
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/remote/protocol.cpp#L1395-L1450">Firebird CSTRING encoding</a>
     */
    public void writeBuffer(final ByteBuf buffer) {
        byteBuf.writeInt(buffer.writerIndex());
        byteBuf.writeBytes(buffer);
        writePadding(buffer.writerIndex());
    }
    
    /**
     * Read a string from the buffer.
     *
     * <p>Reads a Firebird protocol {@code String}, encoded as a {@code Buffer}, using the payload character set.</p>
     *
     * @return string decoded using the payload character set
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/remote/protocol.cpp#L1395-L1450">Firebird counted string encoding</a>
     */
    public String readString() {
        return readBuffer().toString(charset);
    }
    
    /**
     * Write a string to the buffer.
     *
     * <p>Writes a Firebird protocol {@code String}, encoded as a {@code Buffer}, using the payload character set.</p>
     *
     * @param value string to write using the payload character set
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/remote/protocol.cpp#L1395-L1450">Firebird counted string encoding</a>
     */
    public void writeString(final String value) {
        writeBuffer(value.getBytes(charset));
    }
    
    /**
     * Skip reserved bytes in the buffer.
     *
     * <p>Skips raw reserved Firebird {@code UCHAR} bytes without interpreting them as a numeric value.</p>
     *
     * @param length number of bytes to skip
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/include/fb_types.h#L57">Firebird UCHAR</a>
     */
    public void skipReserved(final int length) {
        byteBuf.skipBytes(length);
    }
    
    /**
     * Skip padding bytes in the buffer.
     *
     * <p>Skips the 0 to 3 padding bytes used for Firebird XDR opaque data.
     * Padding is not a separate scalar type.</p>
     *
     * @param length length of the data preceding the padding
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/common/xdr.cpp#L547-L587">Firebird XDR padding</a>
     */
    public void skipPadding(final int length) {
        byteBuf.skipBytes(getPadding(length));
    }
    
    /**
     * Write padding bytes to the buffer.
     *
     * <p>Writes 0 to 3 zero bytes to align Firebird XDR opaque data.
     * Padding is not a separate scalar type.</p>
     *
     * @param length length of the data preceding the padding
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/common/xdr.cpp#L547-L587">Firebird XDR padding</a>
     */
    @HighFrequencyInvocation
    public void writePadding(final int length) {
        byteBuf.writeZero(getPadding(length));
    }
    
    /**
     * Calculate the number of padding bytes.
     *
     * <p>Calculates the 0 to 3 padding bytes used for Firebird XDR opaque data.
     * The result is a byte count, not a serialized value.</p>
     *
     * @param length length of the data preceding the padding
     * @return number of bytes needed to align the data to a multiple of 4 bytes
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/common/xdr.cpp#L547-L587">Firebird XDR padding calculation</a>
     */
    public int getPadding(final int length) {
        return (4 - length) & 3;
    }
    
    /**
     * Calculate the total buffer length including padding.
     *
     * <p>Calculates the length of a Firebird {@code CSTRING} wire representation, including its 32-bit length prefix and padding.</p>
     *
     * @param index index of the length prefix in the buffer
     * @return buffer length including the length prefix and padding
     * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/remote/protocol.cpp#L1395-L1450">Firebird CSTRING encoding</a>
     */
    public int getBufferLength(final int index) {
        int length = byteBuf.getInt(index) + 4;
        return length + getPadding(length);
    }
}
