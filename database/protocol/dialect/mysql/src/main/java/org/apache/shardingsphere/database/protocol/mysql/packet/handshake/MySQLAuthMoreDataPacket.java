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

package org.apache.shardingsphere.database.protocol.mysql.packet.handshake;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;

/**
 * MySQL authentication more data packet.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_auth_more_data.html">AuthMoreData</a>
 */
@RequiredArgsConstructor
@Getter
public final class MySQLAuthMoreDataPacket extends MySQLPacket {
    
    /**
     * Header of MySQL auth more data packet.
     */
    public static final int HEADER = 0x01;
    
    private final byte[] pluginData;
    
    public MySQLAuthMoreDataPacket(final MySQLPacketPayload payload) {
        Preconditions.checkArgument(HEADER == payload.readInt1(), "Header of MySQL auth more data packet must be `0x01`.");
        pluginData = payload.readStringEOFByBytes();
    }
    
    @Override
    protected void write(final MySQLPacketPayload payload) {
        throw new UnsupportedSQLOperationException("MySQLAuthMoreDataPacket.write()");
    }
}
