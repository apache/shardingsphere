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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Column definition above MySQL 4.1 packet protocol.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-Protocol::ColumnDefinition41">ColumnDefinition41</a>
 * @see <a href="https://mariadb.com/kb/en/library/resultset/#column-definition-packet">Column definition packet</a>
 */
public final class MySQLColumnDefinition41Packet implements MySQLPacket {
    
    private static final String CATALOG = "def";
    
    private static final int NEXT_LENGTH = 0x0c;
    
    @Getter
    private final int sequenceId;
    
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
    
    public MySQLColumnDefinition41Packet(final int sequenceId, final ResultSetMetaData resultSetMetaData, final int columnIndex) throws SQLException {
        this(sequenceId, resultSetMetaData.getSchemaName(columnIndex), resultSetMetaData.getTableName(columnIndex), resultSetMetaData.getTableName(columnIndex), 
                resultSetMetaData.getColumnLabel(columnIndex), resultSetMetaData.getColumnName(columnIndex), resultSetMetaData.getColumnDisplaySize(columnIndex), 
                MySQLBinaryColumnType.valueOfJDBCType(resultSetMetaData.getColumnType(columnIndex)), resultSetMetaData.getScale(columnIndex));
    }
    
    /*
     * Field description of column definition Packet.
     *
     * @see <a href="https://github.com/apache/shardingsphere/issues/4358"></a>
     */
    public MySQLColumnDefinition41Packet(final int sequenceId, final String schema, final String table, final String orgTable,
                                         final String name, final String orgName, final int columnLength, final MySQLBinaryColumnType columnType, final int decimals) {
        this(sequenceId, 0, schema, table, orgTable, name, orgName, columnLength, columnType, decimals);
    }
    
    public MySQLColumnDefinition41Packet(final int sequenceId, final int flags, final String schema, final String table, final String orgTable,
                                         final String name, final String orgName, final int columnLength, final MySQLBinaryColumnType columnType, final int decimals) {
        this.sequenceId = sequenceId;
        characterSet = MySQLServerInfo.CHARSET;
        this.flags = flags;
        this.schema = schema;
        this.table = table;
        this.orgTable = orgTable;
        this.name = name;
        this.orgName = orgName;
        this.columnLength = columnLength;
        this.columnType = columnType;
        this.decimals = decimals;
    }
    
    public MySQLColumnDefinition41Packet(final MySQLPacketPayload payload) {
        sequenceId = payload.readInt1();
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
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
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
    }
}
