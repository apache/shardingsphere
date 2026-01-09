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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.props.TypedPropertyKey;

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
     * Max union size per datasource for aggregate rewrite.
     * When route units count for a datasource exceeds this value, they will be split into batches.
     */
    MAX_UNION_SIZE_PER_DATASOURCE("max-union-size-per-datasource", String.valueOf(Integer.MAX_VALUE), int.class, false),
    
    /**
     * Whether validate table metadata consistency when application startup or updated.
     */
    CHECK_TABLE_METADATA_ENABLED("check-table-metadata-enabled", String.valueOf(Boolean.FALSE), boolean.class, false),
    
    /**
     * Load table metadata batch size.
     */
    LOAD_TABLE_METADATA_BATCH_SIZE("load-table-metadata-batch-size", String.valueOf(1000), int.class, false),
    
    /**
     * Frontend database protocol for ShardingSphere-Proxy.
     */
    PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE("proxy-frontend-database-protocol-type", null, DatabaseType.class, false),
    
    /**
     * Flush threshold for every record from databases for ShardingSphere-Proxy.
     */
    PROXY_FRONTEND_FLUSH_THRESHOLD("proxy-frontend-flush-threshold", String.valueOf(128), int.class, false),
    
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
     * Less than or equal to 0 means no limitation.
     */
    PROXY_FRONTEND_MAX_CONNECTIONS("proxy-frontend-max-connections", "0", int.class, false),
    
    /**
     * Proxy default start port.
     */
    PROXY_DEFAULT_PORT("proxy-default-port", "3307", int.class, true),
    
    /**
     * Proxy Netty backlog size.
     */
    PROXY_NETTY_BACKLOG("proxy-netty-backlog", "1024", int.class, false),
    
    /**
     * CDC server port.
     */
    CDC_SERVER_PORT("cdc-server-port", "33071", int.class, true),
    
    /**
     * Proxy frontend SSL enabled.
     */
    PROXY_FRONTEND_SSL_ENABLED("proxy-frontend-ssl-enabled", String.valueOf(Boolean.FALSE), boolean.class, true),
    
    /**
     * Proxy frontend SSL protocol version.
     */
    PROXY_FRONTEND_SSL_VERSION("proxy-frontend-ssl-version", "TLSv1.2,TLSv1.3", String.class, true),
    
    /**
     * Proxy frontend SSL cipher.
     */
    PROXY_FRONTEND_SSL_CIPHER("proxy-frontend-ssl-cipher", "", String.class, true),
    
    /**
     * Agent plugins enabled.
     */
    AGENT_PLUGINS_ENABLED("agent-plugins-enabled", String.valueOf(Boolean.TRUE), boolean.class, false),
    
    /**
     * Persist schemas to repository.
     */
    PERSIST_SCHEMAS_TO_REPOSITORY_ENABLED("persist-schemas-to-repository-enabled", String.valueOf(Boolean.TRUE), boolean.class, true),
    
    /**
     * Maximum size of Groovy inline expression parsing cache.
     */
    GROOVY_INLINE_EXPRESSION_PARSING_CACHE_MAX_SIZE("groovy-inline-expression-parsing-cache-max-size", "1000", long.class, false),

    /**
     * Whether to enable UDT (User Defined Type) discovery.
     */
    UDT_DISCOVERY_ENABLED("udt-discovery-enabled", String.valueOf(Boolean.TRUE), boolean.class, false);
    
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
