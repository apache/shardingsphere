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

package org.apache.shardingsphere.database.protocol.postgresql.packet.identifier;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.postgresql.exception.PostgreSQLProtocolException;

/**
 * Message packet for PostgreSQL.
 * 
 * @see <a href="https://www.postgresql.org/docs/13/protocol-message-formats.html">Message Formats</a>
 */
@RequiredArgsConstructor
@Getter
public enum PostgreSQLMessagePacketType implements PostgreSQLIdentifierTag {
    
    AUTHENTICATION_REQUEST('R'),
    
    PARSE_COMPLETE('1'),
    
    BIND_COMPLETE('2'),
    
    COMMAND_COMPLETE('C'),
    
    ROW_DESCRIPTION('T'),
    
    DATA_ROW('D'),
    
    READY_FOR_QUERY('Z'),
    
    CLOSE_COMPLETE('3'),
    
    COPY_DATA('d'),
    
    COPY_COMPLETE('c'),
    
    COPY_FAILURE('f'),
    
    COPY_IN_RESPONSE('G'),
    
    COPY_OUT_RESPONSE('H'),
    
    COPY_BOTH_RESPONSE('W'),
    
    EMPTY_QUERY_RESPONSE('I'),
    
    ERROR_RESPONSE('E'),
    
    FUNCTION_CALL('F'),
    
    FUNCTION_CALL_RESPONSE('V'),
    
    GSS_RESPONSE('p'),
    
    NOTICE_RESPONSE('N'),
    
    NOTIFICATION_RESPONSE('A'),
    
    SASL_INITIAL_RESPONSE('p'),
    
    SASL_RESPONSE('p'),
    
    BACKEND_KEY_DATA('K'),
    
    NEGOTIATE_PROTOCOL_VERSION('v'),
    
    NO_DATA('n'),
    
    PARAMETER_DESCRIPTION('t'),
    
    PARAMETER_STATUS('S'),
    
    PASSWORD_MESSAGE('p'),
    
    PORTAL_SUSPENDED('s');
    
    private final char value;
    
    /**
     * Value of integer.
     *
     * @param value integer value
     * @return command packet type enum
     * @throws PostgreSQLProtocolException PostgreSQL protocol exception
     */
    public static PostgreSQLMessagePacketType valueOf(final int value) {
        for (PostgreSQLMessagePacketType each : values()) {
            if (value == each.value) {
                return each;
            }
        }
        throw new PostgreSQLProtocolException("Can not find `%s` in PostgreSQL identifier tag type.", value);
    }
}
