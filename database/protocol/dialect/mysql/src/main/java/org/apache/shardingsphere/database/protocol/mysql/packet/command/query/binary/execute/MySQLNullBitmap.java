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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute;

import lombok.Getter;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;

/**
 * Null bitmap for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_binary_resultset.html#sect_protocol_binary_resultset_row_null_bitmap">NULL-Bitmap</a>
 */
public final class MySQLNullBitmap {
    
    private final int offset;
    
    @Getter
    private final int[] nullBitmap;
    
    public MySQLNullBitmap(final int columnsNumbers, final int offset) {
        this.offset = offset;
        nullBitmap = new int[calculateLength(columnsNumbers, offset)];
    }
    
    public MySQLNullBitmap(final int columnNumbers, final MySQLPacketPayload payload) {
        offset = 0;
        nullBitmap = new int[calculateLength(columnNumbers, 0)];
        fillBitmap(payload);
    }
    
    private void fillBitmap(final MySQLPacketPayload payload) {
        for (int i = 0; i < nullBitmap.length; i++) {
            nullBitmap[i] = payload.readInt1();
        }
    }
    
    private int calculateLength(final int columnsNumbers, final int offset) {
        return (columnsNumbers + offset + 7) / 8;
    }
    
    /**
     * Judge parameter is null or not null.
     *
     * @param index column index
     * @return parameter is null or not null
     */
    public boolean isNullParameter(final int index) {
        return (nullBitmap[getBytePosition(index)] & (1 << getBitPosition(index))) != 0;
    }
    
    /**
     * Set null bit.
     *
     * @param index column index
     */
    public void setNullBit(final int index) {
        nullBitmap[getBytePosition(index)] |= 1 << getBitPosition(index);
    }
    
    private int getBytePosition(final int index) {
        return (index + offset) / 8;
    }
    
    private int getBitPosition(final int index) {
        return (index + offset) % 8;
    }
}
