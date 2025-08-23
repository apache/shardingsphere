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

package org.apache.shardingsphere.database.protocol.mysql.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.constant.AuthenticationMethod;

/**
 * Authentication method for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_authentication_methods.html">Authentication Method</a>
 */
@RequiredArgsConstructor
@Getter
public enum MySQLAuthenticationMethod implements AuthenticationMethod {
    
    OLD_PASSWORD("mysql_old_password"),
    
    NATIVE("mysql_native_password"),
    
    CLEAR_TEXT("mysql_clear_password"),
    
    WINDOWS_NATIVE("authentication_windows_client"),
    
    SHA256("sha256_password"),
    
    CACHING_SHA2_PASSWORD("caching_sha2_password");
    
    private final String methodName;
}
