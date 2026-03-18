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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacketType;
import org.apache.shardingsphere.database.protocol.postgresql.exception.PostgreSQLProtocolException;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;

import java.util.EnumSet;
import java.util.Set;

/**
 * Command packet type for PostgreSQL.
 * 
 * @see <a href="https://www.postgresql.org/docs/13/protocol-message-formats.html">Message Formats</a>
 */
@RequiredArgsConstructor
@Getter
public enum PostgreSQLCommandPacketType implements CommandPacketType, PostgreSQLIdentifierTag {
    
    PASSWORD('p'),
    
    SIMPLE_QUERY('Q'),
    
    PARSE_COMMAND('P'),
    
    BIND_COMMAND('B'),
    
    DESCRIBE_COMMAND('D'),
    
    EXECUTE_COMMAND('E'),
    
    SYNC_COMMAND('S'),
    
    CLOSE_COMMAND('C'),
    
    FLUSH_COMMAND('H'),
    
    TERMINATE('X');
    
    private static final Set<PostgreSQLCommandPacketType> EXTENDED_PROTOCOL_PACKET_TYPES = EnumSet.of(
            PARSE_COMMAND, BIND_COMMAND, DESCRIBE_COMMAND, EXECUTE_COMMAND, SYNC_COMMAND, CLOSE_COMMAND, FLUSH_COMMAND);
    
    private final char value;
    
    /**
     * Value of integer.
     *
     * @param value integer value
     * @return command packet type enum
     * @throws PostgreSQLProtocolException PostgreSQL protocol exception
     */
    public static PostgreSQLCommandPacketType valueOf(final int value) {
        for (PostgreSQLCommandPacketType each : values()) {
            if (value == each.value) {
                return each;
            }
        }
        throw new PostgreSQLProtocolException("Can not find `%s` in PostgreSQL command packet type.", value);
    }
    
    /**
     * Check if the packet type is extended protocol packet type.
     *
     * @param commandPacketType command packet type
     * @return is extended protocol packet type
     */
    public static boolean isExtendedProtocolPacketType(final CommandPacketType commandPacketType) {
        return EXTENDED_PROTOCOL_PACKET_TYPES.contains(commandPacketType);
    }
}
