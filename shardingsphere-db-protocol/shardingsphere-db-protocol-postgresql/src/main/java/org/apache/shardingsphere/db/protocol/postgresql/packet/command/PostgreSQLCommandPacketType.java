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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.packet.CommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;

/**
 * Command packet type for PostgreSQL.
 * 
 * @see <a href="https://www.postgresql.org/docs/13/protocol-message-formats.html">Message Formats</a>
 */
@RequiredArgsConstructor
@Getter
public enum PostgreSQLCommandPacketType implements CommandPacketType, PostgreSQLIdentifierTag {
    
    SIMPLE_QUERY('Q'),
    
    PARSE_COMMAND('P'),
    
    BIND_COMMAND('B'),
    
    DESCRIBE_COMMAND('D'),
    
    EXECUTE_COMMAND('E'),
    
    SYNC_COMMAND('S'),
    
    CLOSE_COMMAND('C'),
    
    FLUSH_COMMAND('H'),
    
    TERMINATE('X');
    
    private final char value;
    
    /**
     * Value of integer.
     * 
     * @param value integer value
     * @return command packet type enum
     */
    public static PostgreSQLCommandPacketType valueOf(final int value) {
        for (PostgreSQLCommandPacketType each : values()) {
            if (value == each.value) {
                return each;
            }
        }
        throw new IllegalArgumentException(String.format("Cannot find '%s' in PostgreSQL command packet type", value));
    }
}
