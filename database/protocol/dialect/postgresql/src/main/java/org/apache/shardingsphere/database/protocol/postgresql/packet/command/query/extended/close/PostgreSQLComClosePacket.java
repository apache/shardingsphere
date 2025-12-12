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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.close;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.postgresql.exception.PostgreSQLProtocolException;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;

/**
 * Command close packet for PostgreSQL.
 */
@Getter
public final class PostgreSQLComClosePacket extends PostgreSQLCommandPacket {
    
    private final Type type;
    
    private final String name;
    
    public PostgreSQLComClosePacket(final PostgreSQLPacketPayload payload) {
        payload.readInt4();
        type = Type.valueOf((char) payload.readInt1());
        name = payload.readStringNul();
    }
    
    @Override
    protected void write(final PostgreSQLPacketPayload payload) {
    }
    
    @Override
    public PostgreSQLIdentifierTag getIdentifier() {
        return PostgreSQLCommandPacketType.CLOSE_COMMAND;
    }
    
    @RequiredArgsConstructor
    public enum Type {
        
        PREPARED_STATEMENT('S'),
        
        PORTAL('P');
        
        private final char value;
        
        /**
         * Value of type.
         *
         * @param type type char
         * @return type
         * @throws PostgreSQLProtocolException PostgreSQL protocol exception
         */
        public static Type valueOf(final char type) {
            for (Type each : values()) {
                if (type == each.value) {
                    return each;
                }
            }
            throw new PostgreSQLProtocolException("Close type must be 'S' or 'P'. Got '%c'.", type);
        }
    }
}
