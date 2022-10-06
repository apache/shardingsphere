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

package org.apache.shardingsphere.db.protocol.mysql.packet.binlog;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

/**
 * Abstract MySQL binlog event packet.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Slf4j
public abstract class AbstractMySQLBinlogEventPacket implements MySQLPacket, MySQLBinlogEventPacket {
    
    private final MySQLBinlogEventHeader binlogEventHeader;
    
    @Override
    public final void write(final MySQLPacketPayload payload) {
        binlogEventHeader.write(payload);
        writeEvent(payload);
    }
    
    /**
     * Write event body packet to byte buffer.
     *
     * @param payload packet payload to be written
     */
    protected abstract void writeEvent(MySQLPacketPayload payload);
    
    protected int getRemainBytesLength(final MySQLPacketPayload payload) {
        // minus checksum bytes, add seq id 1 byte, statusCode 1 byte(not include at event size)
        int alreadyReadIndex = binlogEventHeader.getEventSize() + 2 - binlogEventHeader.getChecksumLength();
        if (payload.getByteBuf().readerIndex() > alreadyReadIndex) {
            return -1;
        }
        return alreadyReadIndex - payload.getByteBuf().readerIndex();
    }
    
    @Override
    public final int getSequenceId() {
        return 0;
    }
}
