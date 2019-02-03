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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.DataHeaderPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Column definition above MySQL 4.1 packet protocol.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-Protocol::ColumnDefinition41">ColumnDefinition41</a>
 *
 * @author zhangliang
 * @author zhangyonglun
 */
public final class ColumnDefinition41Packet implements MySQLPacket {
    
    private static final String CATALOG = "def";
    
    private static final int NEXT_LENGTH = 0x0c;
    
    @Getter
    private final int sequenceId;
    
    private final int characterSet;
    
    private final int flags;
    
    private final String schema;
    
    private final String table;
    
    private final String orgTable;
    
    @Getter
    @Setter
    private String name;
    
    private final String orgName;
    
    private final int columnLength;
    
    @Getter
    private final MySQLColumnType mySQLColumnType;
    
    private final int decimals;
    
    public ColumnDefinition41Packet(final int sequenceId, final ResultSetMetaData resultSetMetaData, final int columnIndex) throws SQLException {
        this(sequenceId, resultSetMetaData.getSchemaName(columnIndex), resultSetMetaData.getTableName(columnIndex), resultSetMetaData.getTableName(columnIndex), 
                resultSetMetaData.getColumnLabel(columnIndex), resultSetMetaData.getColumnName(columnIndex), resultSetMetaData.getColumnDisplaySize(columnIndex), 
                MySQLColumnType.valueOfJDBCType(resultSetMetaData.getColumnType(columnIndex)), resultSetMetaData.getScale(columnIndex));
    }
    
    public ColumnDefinition41Packet(final int sequenceId, final String schema, final String table, final String orgTable,
                                    final String name, final String orgName, final int columnLength, final MySQLColumnType mySQLColumnType, final int decimals) {
        this.sequenceId = sequenceId;
        this.characterSet = MySQLServerInfo.CHARSET;
        this.flags = 0;
        this.schema = schema;
        this.table = table;
        this.orgTable = orgTable;
        this.name = name;
        this.orgName = orgName;
        this.columnLength = columnLength;
        this.mySQLColumnType = mySQLColumnType;
        this.decimals = decimals;
    }
    
    public ColumnDefinition41Packet(final DataHeaderPacket dataHeaderPacket) {
        this.sequenceId = dataHeaderPacket.getSequenceId();
        this.characterSet = MySQLServerInfo.CHARSET;
        this.flags = 0;
        this.schema = dataHeaderPacket.getSchema();
        this.table = dataHeaderPacket.getTable();
        this.orgTable = dataHeaderPacket.getOrgTable();
        this.name = dataHeaderPacket.getName();
        this.orgName = dataHeaderPacket.getOrgName();
        this.columnLength = dataHeaderPacket.getColumnLength();
        this.mySQLColumnType = MySQLColumnType.valueOfJDBCType(dataHeaderPacket.getColumnType());
        this.decimals = dataHeaderPacket.getDecimals();
    }
    
    public ColumnDefinition41Packet(final MySQLPacketPayload payload) {
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
        mySQLColumnType = MySQLColumnType.valueOf(payload.readInt1());
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
        payload.writeInt1(mySQLColumnType.getValue());
        payload.writeInt2(flags);
        payload.writeInt1(decimals);
        payload.writeReserved(2);
    }
}
