/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.proxy.transport.mysql.packet.command.statement.execute;

import io.shardingjdbc.proxy.transport.mysql.constant.ColumnType;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingjdbc.proxy.transport.mysql.packet.command.text.query.ColumnDefinition41Packet;
import lombok.Getter;

import java.util.List;

/**
 * Binary result set row packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/binary-protocol-resultset-row.html">Binary Protocol Resultset Row</a>
 *
 * @author zhangyonglun
 */
@Getter
public final class BinaryResultSetRowPacket extends MySQLPacket {
    
    private static final int PACKET_HEADER = 0x00;
    
    private final int numColumns;
    
    private final List<Object> data;
    
    private final List<ColumnDefinition41Packet> columnDefinition41PacketList;
    
    public BinaryResultSetRowPacket(final int sequenceId, final int numColumns, final List<Object> data, final List<ColumnDefinition41Packet> columnDefinition41PacketList) {
        super(sequenceId);
        this.numColumns = numColumns;
        this.data = data;
        this.columnDefinition41PacketList = columnDefinition41PacketList;
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt1(PACKET_HEADER);
        
        int bitmapBytes = (numColumns + 7 + 2) / 8;
        int[] nullBitmap = new int[bitmapBytes];
        for (int each : nullBitmap) {
            mysqlPacketPayload.writeInt1(each);
        }
        
        for (int i = 0; i < numColumns; i++) {
            ColumnType columnType = columnDefinition41PacketList.get(i).getColumnType();
            if (null == data.get(i)) {
                setNullBit(nullBitmap, i);
            } else {
                // TODO add more types
                if (ColumnType.MYSQL_TYPE_LONGLONG == columnType) {
                    mysqlPacketPayload.writeInt8(Long.parseLong(data.get(i).toString()));
                } else if (ColumnType.MYSQL_TYPE_LONG == columnType) {
                    mysqlPacketPayload.writeInt4(Integer.parseInt(data.get(i).toString()));
                } else {
                    mysqlPacketPayload.writeStringLenenc(data.get(i).toString());
                }
            }
        }
    }
    
    private void setNullBit(final int[] nullBitmap, final int index) {
        int bytePos = (index + 2) / 8;
        int bitPos = (index + 2) % 8;
        nullBitmap[bytePos] = 1 << bitPos;
    }
}
