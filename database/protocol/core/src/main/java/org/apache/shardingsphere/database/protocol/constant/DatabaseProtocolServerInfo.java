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

package org.apache.shardingsphere.database.protocol.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.version.DialectProtocolVersionOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database protocol server info.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseProtocolServerInfo {
    
    private static final String SERVER_INFORMATION_PATTERN = "%s-ShardingSphere-Proxy %s";
    
    private static final Map<String, String> SERVER_INFORMATION_MAP = new ConcurrentHashMap<>();
    
    /**
     * Set protocol version.
     *
     * @param databaseName database name
     * @param protocolVersion protocol version
     */
    public static void setProtocolVersion(final String databaseName, final String protocolVersion) {
        SERVER_INFORMATION_MAP.put(databaseName, String.format(SERVER_INFORMATION_PATTERN, protocolVersion, CommonConstants.PROXY_VERSION.get()));
    }
    
    /**
     * Get protocol version.
     *
     * @param databaseName database name
     * @param databaseType database type
     * @return protocol version
     */
    public static String getProtocolVersion(final String databaseName, final DatabaseType databaseType) {
        return null == databaseName ? getDefaultProtocolVersion(databaseType) : SERVER_INFORMATION_MAP.getOrDefault(databaseName, getDefaultProtocolVersion(databaseType));
    }
    
    /**
     * Get default protocol version.
     *
     * @param databaseType database type
     * @return default protocol version
     */
    public static String getDefaultProtocolVersion(final DatabaseType databaseType) {
        DialectProtocolVersionOption protocolVersionOption = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getProtocolVersionOption();
        return String.format(SERVER_INFORMATION_PATTERN, protocolVersionOption.getDefaultVersion(), CommonConstants.PROXY_VERSION.get());
    }
}
