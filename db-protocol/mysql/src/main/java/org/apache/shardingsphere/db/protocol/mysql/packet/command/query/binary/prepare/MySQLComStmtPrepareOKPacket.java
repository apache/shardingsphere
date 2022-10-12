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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

/**
 * COM_STMT_PREPARE_OK packet for MySQL.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-stmt-prepare-response.html#packet-COM_STMT_PREPARE_OK">COM_STMT_PREPARE_OK</a>
 */
@RequiredArgsConstructor
public final class MySQLComStmtPrepareOKPacket implements MySQLPacket {
    
    private static final int STATUS = 0x00;
    
    @Getter
    private final int sequenceId;
    
    private final int statementId;
    
    private final int columnCount;
    
    private final int parameterCount;
    
    private final int warningCount;
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(STATUS);
        payload.writeInt4(statementId);
        // TODO Column Definition Block should be added in future when the metadata of the columns is cached.
        payload.writeInt2(columnCount);
        payload.writeInt2(parameterCount);
        payload.writeReserved(1);
        payload.writeInt2(warningCount);
    }
}
