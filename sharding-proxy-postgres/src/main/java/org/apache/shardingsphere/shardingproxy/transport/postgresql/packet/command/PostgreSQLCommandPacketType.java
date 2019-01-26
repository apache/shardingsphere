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
    
    Backend_Key_Data('K'),
    
    Bind('B'),
    
    Bind_Complete('2'),
    
    Close('C'),
    
    Close_Complete('3'),
    
    Command_Complete('C'),
    
    Copy_Data('d'),
    
    Copy_Done('c'),
    
    Copy_Fail('f'),
    
    Copy_In_Response('G'),
    
    Copy_Out_Response('H'),
    
    Copy_Both_Response('W'),
    
    Data_Row('D'),
    
    Describe('D'),
    
    Empty_Query_Response('I'),
    
    Error_Response('E'),
    
    Execute('E'),
    
    Flush('H'),
    
    Function_Call('F'),
    
    Function_Call_Response('V'),
    
    GSS_Response('p'),
    
    Negotiate_Protocol_Version('v'),
    
    No_Data('n'),
    
    Notice_Response('N'),
    
    Notification_Response('A'),
    
    Parameter_Description('t'),
    
    Parameter_Status('S'),
    
    Parse('P'),
    
    Parse_Complete('1'),
    
    Password_Message('p'),
    
    Portal_Suspended('s'),
    
    Query('Q'),
    
    Ready_For_Query('Z'),
    
    Row_Description('T'),
    
    SASL_Initial_Response('p'),
    
    SASL_Response('p'),
    
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
