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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row;

import java.util.BitSet;

import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;

/**
 * MySQL binlog bitmap.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/bitmaps.html">Bitmaps</a>
 */
public final class MySQLBinlogBitmap {
    
    private final BitSet bitSet;
    
    public MySQLBinlogBitmap(final int length, final MySQLPacketPayload payload) {
        this.bitSet = new BitSet(length);
        fillBitmap(length, payload);
    }
    
    private void fillBitmap(final int length, final MySQLPacketPayload payload) {
        for (int bit = 0; bit < length; bit += 8) {
            int flag = payload.readInt1();
            if (0 != flag) {
                for (int i = 0; i < 8; i++) {
                    if (0 != (flag & (0x01 << i))) {
                        bitSet.set(bit + i);
                    }
                }
            }
        }
    }
    
    /**
     * Whether contain bit of index in bitmap.
     *
     * @param index index of bit
     * @return {@code true} if contain bit, otherwise {@code false}
     */
    public boolean containBit(final int index) {
        return bitSet.get(index);
    }
}
