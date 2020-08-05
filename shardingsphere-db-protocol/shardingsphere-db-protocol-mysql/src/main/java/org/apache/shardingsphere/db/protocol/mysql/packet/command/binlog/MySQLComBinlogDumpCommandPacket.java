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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.binlog;

import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import lombok.Getter;
import lombok.ToString;

/**
 * COM_BINLOG_DUMP command packet for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-binlog-dump.html">COM_BINLOG_DUMP</a>
 */
@Getter
@ToString
public final class MySQLComBinlogDumpCommandPacket extends MySQLCommandPacket {
    
    /**
     * if there is no more event to send a EOF_Packet instead of blocking the connection.
     */
    public static final int BINLOG_DUMP_NON_BLOCK = 0x01;
    
    private final int binlogPos;
    
    private final int flags;
    
    private final int serverId;
    
    private final String binlogFilename;
    
    public MySQLComBinlogDumpCommandPacket(final int binlogPos, final int serverId, final String binlogFilename) {
        super(MySQLCommandPacketType.COM_BINLOG_DUMP);
        this.binlogPos = binlogPos;
        flags = 0;
        this.serverId = serverId;
        this.binlogFilename = binlogFilename;
    }
    
    public MySQLComBinlogDumpCommandPacket(final MySQLPacketPayload payload) {
        super(MySQLCommandPacketType.COM_BINLOG_DUMP);
        binlogPos = payload.readInt4();
        flags = payload.readInt2();
        serverId = payload.readInt4();
        binlogFilename = payload.readStringEOF();
    }
    
    @Override
    protected void doWrite(final MySQLPacketPayload payload) {
        payload.writeInt4(binlogPos);
        payload.writeInt2(flags);
        payload.writeInt4(serverId);
        payload.writeStringEOF(binlogFilename);
    }
}
