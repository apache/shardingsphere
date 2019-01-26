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

package org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PostgreSQL command packet type.
 *
 * @author zhangyonglun
 */
@RequiredArgsConstructor
@Getter
public enum PostgreSQLCommandPacketType {
    
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
    
    BACKEND_KEY_DATA('K'),
    
    BIND('B'),
    
    BIND_COMPLETE('2'),
    
    CLOSE('C'),
    
    CLOSE_COMPLETE('3'),
    
    COMMAND_COMPLETE('C'),
    
    COPY_DATA('d'),
    
    COPY_DONE('c'),
    
    COPY_FAIL('f'),
    
    COPY_IN_RESPONSE('G'),
    
    COPY_OUT_RESPONSE('H'),
    
    COPY_BOTH_RESPONSE('W'),
    
    DATA_ROW('D'),
    
    DESCRIBE('D'),
    
    EMPTY_QUERY_RESPONSE('I'),
    
    ERROR_RESPONSE('E'),
    
    EXECUTE('E'),
    
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
    
    PARSE('P'),
    
    PARSE_COMPLETE('1'),
    
    PASSWORD_MESSAGE('p'),
    
    PORTAL_SUSPENDED('s'),
    
    QUERY('Q'),
    
    READY_FOR_QUERY('Z'),
    
    ROW_DESCRIPTION('T'),
    
    SASL_INITIAL_RESPONSE('p'),
    
    SASL_RESPONSE('p'),
    
    SYNC('S'),
    
    TERMINATE('X');
    
    private final int value;
    
    /**
     * Value of integer.
     * 
     * @param value integer value
     * @return command packet type enum
     */
    public static PostgreSQLCommandPacketType valueOf(final int value) {
        for (PostgreSQLCommandPacketType each : PostgreSQLCommandPacketType.values()) {
            if (value == each.value) {
                return each;
            }
        }
        throw new IllegalArgumentException(String.format("Cannot find '%s' in PostgreSQL command packet type", value));
    }
}
