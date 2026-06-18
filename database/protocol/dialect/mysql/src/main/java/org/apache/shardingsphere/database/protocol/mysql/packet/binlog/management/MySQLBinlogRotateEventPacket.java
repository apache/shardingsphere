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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.management;

import lombok.Getter;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.AbstractMySQLBinlogEventPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.MySQLBinlogEventHeader;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;

/**
 * MySQL binlog rotate event packet.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/classbinary__log_1_1Rotate__event.html">ROTATE_EVENT</a>
 */
@Getter
public final class MySQLBinlogRotateEventPacket extends AbstractMySQLBinlogEventPacket {
    
    private final long position;
    
    private final String nextBinlogName;
    
    public MySQLBinlogRotateEventPacket(final MySQLBinlogEventHeader binlogEventHeader, final long position, final String nextBinlogName) {
        super(binlogEventHeader);
        this.position = position;
        this.nextBinlogName = nextBinlogName;
    }
    
    public MySQLBinlogRotateEventPacket(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        super(binlogEventHeader);
        position = payload.readInt8();
        nextBinlogName = payload.readStringFix(getRemainBytesLength(payload));
    }
    
    @Override
    protected void writeEvent(final MySQLPacketPayload payload) {
        payload.writeInt8(position);
        payload.writeStringEOF(nextBinlogName);
    }
}
