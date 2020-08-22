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

package org.apache.shardingsphere.db.protocol.mysql.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * ShardingSphere-Proxy's information for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLServerInfo {
    
    /**
     * Protocol version is always 0x0A.
     */
    public static final int PROTOCOL_VERSION = 0x0A;
    
    /**
     * Charset code 0x21 is utf8_general_ci.
     */
    public static final int CHARSET = 0x21;
    
    private static final String DEFAULT_MYSQL_VERSION = "8.0.20";
    
    private static final String PROXY_VERSION = "5.0.0-RC1";
    
    private static final String SERVER_VERSION_PATTERN = "%s-ShardingSphere-Proxy %s";
    
    private static volatile String serverVersion;
    
    /**
     * Set server version.
     *
     * @param serverVersion server version
     */
    public static synchronized void setServerVersion(final String serverVersion) {
        MySQLServerInfo.serverVersion = null == serverVersion ? null : String.format(SERVER_VERSION_PATTERN, serverVersion, PROXY_VERSION);
    }
    
    /**
     * Get current server version.
     *
     * @return server version
     */
    public static String getServerVersion() {
        return null == serverVersion ? String.format(SERVER_VERSION_PATTERN, DEFAULT_MYSQL_VERSION, PROXY_VERSION) : serverVersion;
    }
}
