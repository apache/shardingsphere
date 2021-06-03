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
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

/**
 * Command complete packet for PostgreSQL.
 */
@RequiredArgsConstructor
public final class PostgreSQLCommandCompletePacket implements PostgreSQLIdentifierPacket {
    
    private final String sqlCommand;
    
    private final long rowCount;
    
    public PostgreSQLCommandCompletePacket() {
        this("", 0);
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
        switch (sqlCommand) {
            case "BEGIN":
            case "COMMIT":
            case "ROLLBACK":
                payload.writeStringNul(sqlCommand);
                return;
            default:
        }
        String delimiter = "INSERT".equals(sqlCommand) ? " 0 " : " ";
        payload.writeStringNul(String.join(delimiter, sqlCommand, Long.toString(rowCount)));
    }
    
    @Override
    public PostgreSQLIdentifierTag getIdentifier() {
        return PostgreSQLMessagePacketType.COMMAND_COMPLETE;
    }
}
