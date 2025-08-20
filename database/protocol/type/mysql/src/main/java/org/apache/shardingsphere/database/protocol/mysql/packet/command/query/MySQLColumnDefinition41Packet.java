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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;

/**
 * Column definition above MySQL 4.1 packet protocol.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query_response_text_resultset_column_definition.html">ColumnDefinition41</a>
 * @see <a href="https://mariadb.com/kb/en/library/resultset/#column-definition-packet">Column definition packet</a>
 */
@RequiredArgsConstructor
public final class MySQLColumnDefinition41Packet extends MySQLPacket {
    
    private static final String CATALOG = "def";
    
    private static final int NEXT_LENGTH = 0x0c;
    
    private final int characterSet;
    
    private final int flags;
    
    private final String schema;
    
    private final String table;
    
    private final String orgTable;
    
    private final String name;
    
    private final String orgName;
    
    private final int columnLength;
    
    private final MySQLBinaryColumnType columnType;
    
    private final int decimals;
    
    private final boolean containDefaultValues;
    
    /*
     * Field description of column definition Packet.
     *
     * @see <a href="https://github.com/apache/shardingsphere/issues/4358"></a>
     */
    public MySQLColumnDefinition41Packet(final int characterSet, final String schema, final String table, final String orgTable,
                                         final String name, final String orgName, final int columnLength, final MySQLBinaryColumnType columnType,
                                         final int decimals, final boolean containDefaultValues) {
        this(characterSet, 0, schema, table, orgTable, name, orgName, columnLength, columnType, decimals, containDefaultValues);
    }
    
    public MySQLColumnDefinition41Packet(final MySQLPacketPayload payload) {
        Preconditions.checkArgument(CATALOG.equals(payload.readStringLenenc()));
        schema = payload.readStringLenenc();
        table = payload.readStringLenenc();
        orgTable = payload.readStringLenenc();
        name = payload.readStringLenenc();
        orgName = payload.readStringLenenc();
        Preconditions.checkArgument(NEXT_LENGTH == payload.readIntLenenc());
        characterSet = payload.readInt2();
        columnLength = payload.readInt4();
        columnType = MySQLBinaryColumnType.valueOf(payload.readInt1());
        flags = payload.readInt2();
        decimals = payload.readInt1();
        payload.skipReserved(2);
        containDefaultValues = false;
    }
    
    @Override
    protected void write(final MySQLPacketPayload payload) {
        payload.writeStringLenenc(CATALOG);
        payload.writeStringLenenc(schema);
        payload.writeStringLenenc(table);
        payload.writeStringLenenc(orgTable);
        payload.writeStringLenenc(name);
        payload.writeStringLenenc(orgName);
        payload.writeIntLenenc(NEXT_LENGTH);
        payload.writeInt2(characterSet);
        payload.writeInt4(columnLength);
        payload.writeInt1(columnType.getValue());
        payload.writeInt2(flags);
        payload.writeInt1(decimals);
        payload.writeReserved(2);
        if (containDefaultValues) {
            payload.writeIntLenenc(0);
            payload.writeStringLenenc("");
        }
    }
}
