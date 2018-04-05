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

package io.shardingjdbc.proxy.transport.mysql.packet.command.text.query;

import com.google.common.base.Preconditions;
import io.shardingjdbc.proxy.transport.mysql.constant.ColumnType;
import io.shardingjdbc.proxy.transport.mysql.constant.ServerInfo;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacketPayload;
import lombok.Getter;

/**
 * Column definition above MySQL 4.1 packet protocol.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-Protocol::ColumnDefinition41">ColumnDefinition41</a>
 *
 * @author zhangliang
 */
@Getter
public final class ColumnDefinition41Packet extends MySQLPacket {
    
    private final String catalog = "def";
    
    private final int nextLength = 0x0c;
    
    private final int characterSet = ServerInfo.CHARSET;
    
    private final int flags = 0;
    
    private final String schema;
    
    private final String table;
    
    private final String orgTable;
    
    private final String name;
    
    private final String orgName;
    
    private final int columnLength;
    
    private final ColumnType columnType;
    
    private final int decimals;
    
    public ColumnDefinition41Packet(final int sequenceId, final String schema, final String table, final String orgTable, 
                                    final String name, final String orgName, final int columnLength, final ColumnType columnType, final int decimals) {
        super(sequenceId);
        this.schema = schema;
        this.table = table;
        this.orgTable = orgTable;
        this.name = name;
        this.orgName = orgName;
        this.columnLength = columnLength;
        this.columnType = columnType;
        this.decimals = decimals;
    }
    
    public ColumnDefinition41Packet(final MySQLPacketPayload mysqlPacketPayload) {
        super(mysqlPacketPayload.readInt1());
        Preconditions.checkArgument(catalog.equals(mysqlPacketPayload.readStringLenenc()));
        schema = mysqlPacketPayload.readStringLenenc();
        table = mysqlPacketPayload.readStringLenenc();
        orgTable = mysqlPacketPayload.readStringLenenc();
        name = mysqlPacketPayload.readStringLenenc();
        orgName = mysqlPacketPayload.readStringLenenc();
        Preconditions.checkArgument(nextLength == mysqlPacketPayload.readIntLenenc());
        Preconditions.checkArgument(characterSet == mysqlPacketPayload.readInt2());
        columnLength = mysqlPacketPayload.readInt4();
        columnType = ColumnType.valueOf(mysqlPacketPayload.readInt1());
        Preconditions.checkArgument(flags == mysqlPacketPayload.readInt2());
        decimals = mysqlPacketPayload.readInt1();
        mysqlPacketPayload.skipReserved(2);
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeStringLenenc(catalog);
        mysqlPacketPayload.writeStringLenenc(schema);
        mysqlPacketPayload.writeStringLenenc(table);
        mysqlPacketPayload.writeStringLenenc(orgTable);
        mysqlPacketPayload.writeStringLenenc(name);
        mysqlPacketPayload.writeStringLenenc(orgName);
        mysqlPacketPayload.writeIntLenenc(nextLength);
        mysqlPacketPayload.writeInt2(characterSet);
        mysqlPacketPayload.writeInt4(columnLength);
        mysqlPacketPayload.writeInt1(columnType.getValue());
        mysqlPacketPayload.writeInt2(flags);
        mysqlPacketPayload.writeInt1(decimals);
        mysqlPacketPayload.writeReserved(2);
    }
}
