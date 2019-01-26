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
    
    AUTHENTICATION_Cleartext_Password('R'),
    
    Authentication_MD5_Password('R'),
    
    Authentication_SCM_Credential('R'),
    
    Authentication_GSS('R'),
    
    Authentication_SSPI('R'),
    
    Authentication_GSS_Continue('R'),
    
    Authentication_SASL('R'),
    
    Authentication_SASL_Continue('R'),
    
    Authentication_SASL_Final('R'),
    
    BackendKeyData('K'),
    
    Bind('B'),
    
    BindComplete('2'),
    
    Close('C'),
    
    CloseComplete('3'),
    
    CommandComplete('C'),
    
    CopyData('d'),
    
    CopyDone('c'),
    
    CopyFail('f'),
    
    CopyInResponse('G'),
    
    CopyOutResponse('H'),
    
    CopyBothResponse('W'),
    
    DataRow('D'),
    
    Describe('D'),
    
    EmptyQueryResponse('I'),
    
    ErrorResponse('E'),
    
    Execute('E'),
    
    Flush('H'),
    
    FunctionCall('F'),
    
    FunctionCallResponse('V'),
    
    GSSResponse('p'),
    
    NegotiateProtocolVersion('v'),
    
    NoData('n'),
    
    NoticeResponse('N'),
    
    NotificationResponse('A'),
    
    ParameterDescription('t'),
    
    ParameterStatus('S'),
    
    Parse('P'),
    
    ParseComplete('1'),
    
    PasswordMessage('p'),
    
    PortalSuspended('s'),
    
    Query('Q'),
    
    ReadyForQuery('Z'),
    
    RowDescription('T'),
    
    SASLInitialResponse('p'),
    
    SASLResponse('p'),
    
    Sync('S'),
    
    Terminate('X');
    
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
