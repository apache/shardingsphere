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

package org.apache.shardingsphere.db.protocol.postgresql.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.db.protocol.constant.CommonConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShardingSphere-Proxy's information for PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLServerInfo {
    
    private static final String SERVER_VERSION_PATTERN = "%s-ShardingSphere-Proxy %s";
    
    private static final String DEFAULT_POSTGRESQL_VERSION = "12.3";
    
    private static final Map<String, String> SERVER_VERSIONS = new ConcurrentHashMap<>();
    
    /**
     * Set server version.
     *
     * @param databaseName database name
     * @param serverVersion server version
     */
    public static void setServerVersion(final String databaseName, final String serverVersion) {
        SERVER_VERSIONS.put(databaseName, String.format(SERVER_VERSION_PATTERN, serverVersion, CommonConstants.PROXY_VERSION.get()));
    }
    
    /**
     * Get server version.
     *
     * @param databaseName database name
     * @return server version
     */
    public static String getServerVersion(final String databaseName) {
        return null == databaseName ? getDefaultServerVersion() : SERVER_VERSIONS.getOrDefault(databaseName, getDefaultServerVersion());
    }
    
    private static String getDefaultServerVersion() {
        return String.format(SERVER_VERSION_PATTERN, DEFAULT_POSTGRESQL_VERSION, CommonConstants.PROXY_VERSION.get());
    }
}
