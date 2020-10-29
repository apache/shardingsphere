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

/**
 * Command packet type for PostgreSQL.
 */
@RequiredArgsConstructor
@Getter
public enum PostgreSQLCommandPacketType implements CommandPacketType {
    
    AUTHENTICATION_OK('R'),
    
    AUTHENTICATION_KERBEROS_V5('R'),
    
    AUTHENTICATION_CLEARTEXT_PASSWORD('R'),
    
    AUTHENTICATION_MD5_PASSWORD('R'),
    
    AUTHENTICATION_SCM_CREDENTIAL('R'),
    
    AUTHENTICATION_GSS('R'),
    
    AUTHENTICATION_SSPI('R'),
    
    AUTHENTICATION_GSS_CONTINUE('R'),
    
    AUTHENTICATION_SASL('R'),
    
    AUTHENTICATION_SASL_CONTINUE('R'),
    
    AUTHENTICATION_SASL_FINAL('R'),
    
    QUERY('Q'),
    
    PARSE('P'),
    
    BIND('B'),
    
    DESCRIBE('D'),
    
    EXECUTE('E'),
    
    SYNC('S'),
    
    PARSE_COMPLETE('1'),
    
    BIND_COMPLETE('2'),
    
    ROW_DESCRIPTION('T'),
    
    DATA_ROW('D'),
    
    COMMAND_COMPLETE('C'),
    
    READY_FOR_QUERY('Z'),
    
    CLOSE('C'),
    
    CLOSE_COMPLETE('3'),
    
    BACKEND_KEY_DATA('K'),
    
    COPY_DATA('d'),
    
    COPY_DONE('c'),
    
    COPY_FAIL('f'),
    
    COPY_IN_RESPONSE('G'),
    
    COPY_OUT_RESPONSE('H'),
    
    COPY_BOTH_RESPONSE('W'),
    
    EMPTY_QUERY_RESPONSE('I'),
    
    ERROR_RESPONSE('E'),
    
    FLUSH('H'),
    
    FUNCTION_CALL('F'),
    
    FUNCTION_CALL_RESPONSE('V'),
    
    GSS_RESPONSE('p'),
    
    NEGOTIATE_PROTOCOL_VERSION('v'),
    
    NO_DATA('n'),
    
    NOTICE_RESPONSE('N'),
    
    NOTIFICATION_RESPONSE('A'),
    
    PARAMETER_DESCRIPTION('t'),
    
    PARAMETER_STATUS('S'),
    
    PASSWORD_MESSAGE('p'),
    
    PORTAL_SUSPENDED('s'),

    SASL_INITIAL_RESPONSE('p'),
    
    SASL_RESPONSE('p'),
    
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
