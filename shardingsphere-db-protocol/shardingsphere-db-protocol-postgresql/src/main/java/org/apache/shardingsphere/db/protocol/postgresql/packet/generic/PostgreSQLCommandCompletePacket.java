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

package org.apache.shardingsphere.db.protocol.postgresql.packet.generic;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

/**
 * Command complete packet for PostgreSQL.
 */
@RequiredArgsConstructor
public final class PostgreSQLCommandCompletePacket implements PostgreSQLPacket {
    
    private final String sqlCommand;
    
    private final long rowCount;
    
    public PostgreSQLCommandCompletePacket() {
        this("", 0);
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
        String delimiter = "INSERT".equals(sqlCommand) ? " 0 " : " ";
        payload.writeStringNul(String.join(delimiter, sqlCommand, Long.toString(rowCount)));
    }
    
    @Override
    public char getMessageType() {
        return PostgreSQLCommandPacketType.COMMAND_COMPLETE.getValue();
    }
}
