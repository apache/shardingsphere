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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.query;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * COM_QUERY command packet for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query.html">COM_QUERY</a>
 */
@Getter
@ToString
public final class MySQLComQueryPacket extends MySQLCommandPacket {
    
    private final String sql;
    
    // _binary'
    private final byte[] startPrefix = new byte[]{95, 98, 105, 110, 97, 114, 121, 39};
    
    private final List<Object> parameters = new ArrayList<>();
    
    public MySQLComQueryPacket(final String sql) {
        super(MySQLCommandPacketType.COM_QUERY);
        this.sql = sql;
    }
    
    public MySQLComQueryPacket(final MySQLPacketPayload payload) {
        super(MySQLCommandPacketType.COM_QUERY);
        byte[] bytes = payload.readStringEOFByBytes();
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        try {
            extractBytesParameter(bytes, buf, 0);
            if (buf.readableBytes() > 0) {
                byte[] result = new byte[buf.readableBytes()];
                buf.readBytes(result);
                sql = new String(result, payload.getCharset());
            } else {
                sql = new String(bytes, payload.getCharset());
            }
        } finally {
            ReferenceCountUtil.safeRelease(buf);
        }
    }
    
    @Override
    public void doWrite(final MySQLPacketPayload payload) {
        payload.writeStringEOF(sql);
    }
    
    /*
     * @param source src bytes
     *
     * @param target target buf
     *
     * @param fromIndex start
     */
    private void extractBytesParameter(final byte[] source, final ByteBuf target, final int fromIndex) {
        int startIndex = indexOf(source, startPrefix, fromIndex);
        if (startIndex <= 0) {
            // or parameters is not empty
            if (target.readableBytes() > 0) {
                target.writeBytes(source, fromIndex, source.length - fromIndex);
            }
            return;
        }
        target.writeBytes(source, fromIndex, startIndex - fromIndex);
        int endIndex = endOf(source, startIndex);
        parameters.add(removeEscapeFlag(source, startIndex + startPrefix.length, endIndex));
        // replace bytes to ?
        target.writeByte((byte) 63);
        extractBytesParameter(source, target, endIndex + 1);
    }
    
    /*
     *
     * @param byteBuf source buf
     *
     * @param source target bytes
     *
     * @param from index start
     *
     * @param length
     */
    private byte[] removeEscapeFlag(final byte[] source, final int fromIndex, final int endIndex) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.heapBuffer();
        try {
            int i = fromIndex;
            while (i < endIndex) {
                byte b = source[i];
                if (b == 92 && source[i + 1] == 48) {
                    // \0
                    buf.writeByte((byte) 0);
                    i++;
                } else if (b == 92 || b == 39) {
                    // remove double
                    buf.writeByte(b);
                    i++;
                } else {
                    buf.writeByte(b);
                }
                i++;
            }
            byte[] result = new byte[buf.readableBytes()];
            buf.getBytes(0, result);
            return result;
        } finally {
            ReferenceCountUtil.safeRelease(buf);
        }
        
    }
    
    /*
     * @param source src bytes
     *
     * @param target bytes
     *
     * @param fromIndex start
     *
     * @return index
     */
    private int indexOf(final byte[] source, final byte[] target, final int from) {
        int fromIndex = from;
        if (fromIndex >= source.length) {
            return -1;
        }
        byte first = target[0];
        int max = source.length - target.length;
        if (fromIndex > max) {
            return -1;
        }
        while (fromIndex <= max) {
            /* Look for first byte. */
            if (source[fromIndex] != first) {
                while (++fromIndex <= max && source[fromIndex] != first) {
                    // EmptyBlock: Must have at least one statement.
                    continue;
                }
            }
            
            /* Found first byte, now look at the rest of v2 */
            if (fromIndex <= max) {
                int j = fromIndex + 1;
                int end = j + target.length - 1;
                for (int k = 1; j < end && source[j] == target[k]; j++, k++) {
                    // EmptyBlock: Must have at least one statement.
                    continue;
                }
                if (j == end) {
                    return fromIndex;
                }
            }
            fromIndex++;
        }
        return -1;
    }
    
    /*
     * ****39,44****',**** ****39,41****')****
     *
     * @param source src bytes
     *
     * @param fromIndex start index
     *
     * @return index
     */
    private int endOf(final byte[] source, final int from) {
        // target [39,44|41]
        int fromIndex = from;
        if (fromIndex >= source.length) {
            return -1;
        }
        byte first = (byte) 39;
        int max = source.length - 2;
        if (fromIndex > max) {
            return -1;
        }
        while (fromIndex <= max) {
            /* Look for first byte. */
            if (source[fromIndex] != first) {
                while (++fromIndex <= max && source[fromIndex] != first) {
                    // EmptyBlock: Must have at least one statement.
                    continue;
                }
            }
            
            /* Found first byte, now look at the second. */
            if (fromIndex <= max) {
                int j = fromIndex + 1;
                if (source[j] != 39 && source[fromIndex - 1] != 39 && (source[j] == 44 || source[j] == 41)) {
                    return fromIndex;
                }
            }
            fromIndex++;
        }
        return -1;
    }
}
