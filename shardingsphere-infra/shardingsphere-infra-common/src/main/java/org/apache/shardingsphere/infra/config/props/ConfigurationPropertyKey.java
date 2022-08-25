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

package org.apache.shardingsphere.infra.config.props;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.props.TypedPropertyKey;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Typed property key of configuration.
 */
@RequiredArgsConstructor
@Getter
public enum ConfigurationPropertyKey implements TypedPropertyKey {
    
    /**
     * Whether show SQL in log.
     */
    SQL_SHOW("sql-show", String.valueOf(Boolean.FALSE), boolean.class, false),
    
    /**
     * Whether show SQL details in simple style.
     */
    SQL_SIMPLE("sql-simple", String.valueOf(Boolean.FALSE), boolean.class, false),
    
    /**
     * The max thread size of worker group to execute SQL.
     */
    KERNEL_EXECUTOR_SIZE("kernel-executor-size", String.valueOf(0), int.class, true),
    
    /**
     * Max opened connection size for each query.
     */
    MAX_CONNECTIONS_SIZE_PER_QUERY("max-connections-size-per-query", String.valueOf(1), int.class, false),
    
    /**
     * Whether validate table meta data consistency when application startup or updated.
     */
    CHECK_TABLE_METADATA_ENABLED("check-table-metadata-enabled", String.valueOf(Boolean.FALSE), boolean.class, false),
    
    /**
     * Whether enable SQL federation.
     */
    SQL_FEDERATION_ENABLED("sql-federation-enabled", String.valueOf(Boolean.FALSE), boolean.class, false),
    
    /**
     * Frontend database protocol type for ShardingSphere-Proxy.
     */
    PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE("proxy-frontend-database-protocol-type", "", String.class, false),
    
    /**
     * Flush threshold for every records from databases for ShardingSphere-Proxy.
     */
    PROXY_FRONTEND_FLUSH_THRESHOLD("proxy-frontend-flush-threshold", String.valueOf(128), int.class, false),
    
    /**
     * Whether enable hint for ShardingSphere-Proxy.
     */
    PROXY_HINT_ENABLED("proxy-hint-enabled", String.valueOf(Boolean.FALSE), boolean.class, false),
    
    /**
     * Proxy backend query fetch size. A larger value may increase the memory usage of ShardingSphere Proxy.
     * The default value is -1, which means set the minimum value for different JDBC drivers.
     */
    PROXY_BACKEND_QUERY_FETCH_SIZE("proxy-backend-query-fetch-size", String.valueOf(-1), int.class, false),
    
    /**
     * Proxy frontend executor size. The default value is 0, which means let Netty decide.
     */
    PROXY_FRONTEND_EXECUTOR_SIZE("proxy-frontend-executor-size", String.valueOf(0), int.class, true),
    
    /**
     * Available options of proxy backend executor suitable: OLAP(default), OLTP. The OLTP option may reduce time cost of writing packets to client, but it may increase the latency of SQL execution
     * and block other clients if client connections are more than {@link ConfigurationPropertyKey#PROXY_FRONTEND_EXECUTOR_SIZE}, especially executing slow SQL.
     */
    PROXY_BACKEND_EXECUTOR_SUITABLE("proxy-backend-executor-suitable", "OLAP", String.class, false),
    
    /**
     * Less than or equal to 0 means no limitation.
     */
    PROXY_FRONTEND_MAX_CONNECTIONS("proxy-frontend-max-connections", "0", int.class, false),
    
    /**
     * Proxy backend driver type..
     */
    PROXY_BACKEND_DRIVER_TYPE("proxy-backend-driver-type", "JDBC", String.class, true),
    
    /**
     * Proxy MySQL default version.
     */
    PROXY_MYSQL_DEFAULT_VERSION("proxy-mysql-default-version", "5.7.22", String.class, false),
    
    /**
     * Proxy default start port.
     */
    PROXY_DEFAULT_PORT("proxy-default-port", "3307", int.class, true),
    
    /**
     * Proxy Netty backlog size.
     */
    PROXY_NETTY_BACKLOG("proxy-netty-backlog", "1024", int.class, false),
    
    /**
     * Proxy instance type.
     */
    PROXY_INSTANCE_TYPE("proxy-instance-type", "Proxy", String.class, true);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
    
    private final boolean rebootRequired;
    
    /**
     * Get property key names.
     *
     * @return collection of key names
     */
    public static Collection<String> getKeyNames() {
        return Arrays.stream(values()).map(ConfigurationPropertyKey::name).collect(Collectors.toList());
    }
}
