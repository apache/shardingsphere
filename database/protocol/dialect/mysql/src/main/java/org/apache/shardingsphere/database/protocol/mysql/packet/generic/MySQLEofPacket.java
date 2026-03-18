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

package org.apache.shardingsphere.database.protocol.mysql.packet.generic;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;

/**
 * EOF packet protocol for MySQL.
 * 
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_eof_packet.html">EOF Packet</a>
 */
@RequiredArgsConstructor
@Getter
public final class MySQLEofPacket extends MySQLPacket {
    
    /**
     * Header of EOF packet.
     */
    public static final int HEADER = 0xfe;
    
    private final int warnings;
    
    private final int statusFlags;
    
    public MySQLEofPacket(final int statusFlags) {
        this(0, statusFlags);
    }
    
    public MySQLEofPacket(final MySQLPacketPayload payload) {
        Preconditions.checkArgument(HEADER == payload.readInt1(), "Header of MySQL EOF packet must be `0xfe`.");
        warnings = payload.readInt2();
        statusFlags = payload.readInt2();
    }
    
    @Override
    protected void write(final MySQLPacketPayload payload) {
        payload.writeInt1(HEADER);
        payload.writeInt2(warnings);
        payload.writeInt2(statusFlags);
    }
}
