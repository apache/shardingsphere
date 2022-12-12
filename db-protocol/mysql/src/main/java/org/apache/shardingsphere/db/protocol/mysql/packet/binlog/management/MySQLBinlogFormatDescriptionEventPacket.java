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

package org.apache.shardingsphere.db.protocol.mysql.packet.binlog.management;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinlogEventType;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.AbstractMySQLBinlogEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.MySQLBinlogEventHeader;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

/**
 * MySQL binlog format description event packet.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/format-description-event.html">FORMAT_DESCRIPTION_EVENT</a>
 * @see <a href="https://dev.mysql.com/worklog/task/?id=2540#tabs-2540-4">WL#2540: Replication event checksums</a>
 */
@Getter
@ToString
public final class MySQLBinlogFormatDescriptionEventPacket extends AbstractMySQLBinlogEventPacket {
    
    private final int binlogVersion = 4;
    
    private final byte[] mysqlServerVersion;
    
    private final int createTimestamp;
    
    private final int eventHeaderLength = 19;
    
    public MySQLBinlogFormatDescriptionEventPacket(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        super(binlogEventHeader);
        Preconditions.checkArgument(binlogVersion == payload.readInt2(), "Binlog version of FORMAT_DESCRIPTION_EVENT should always 4");
        mysqlServerVersion = payload.readStringFixByBytes(50);
        createTimestamp = payload.readInt4();
        Preconditions.checkArgument(eventHeaderLength == payload.readInt1(), "Length of the Binlog Event Header should always be 19.");
        skipTypeHeaderLength(payload);
        skipCheckSums(payload);
    }
    
    /**
     * Type header length is not fixed value, it depends on mysql server version.
     * Because the binlog event type may add in future version.
     * During test, the length in version 5.7.21, the length of FORMAT_DESCRIPTION_EVENT is 95 not 84.
     *
     * @param payload MySQL binlog packet payload
     */
    private void skipTypeHeaderLength(final MySQLPacketPayload payload) {
        payload.skipReserved(MySQLBinlogEventType.FORMAT_DESCRIPTION_EVENT.getValue() - 1);
        int eventLength = payload.readInt1();
        int remainLength = eventLength - 2 - 50 - 4 - 1 - (MySQLBinlogEventType.FORMAT_DESCRIPTION_EVENT.getValue() - 1) - 1;
        payload.skipReserved(remainLength);
    }
    
    private void skipCheckSums(final MySQLPacketPayload payload) {
        int checksumAlgorithmFlag = payload.readInt1();
        if (1 == checksumAlgorithmFlag) {
            payload.skipReserved(4);
        }
    }
    
    @Override
    protected void writeEvent(final MySQLPacketPayload payload) {
        // TODO
    }
}
