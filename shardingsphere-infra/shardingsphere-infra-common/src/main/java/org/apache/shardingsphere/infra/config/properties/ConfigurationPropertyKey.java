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

package org.apache.shardingsphere.infra.config.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.properties.TypedPropertyKey;

/**
 * Typed property key of configuration.
 */
@RequiredArgsConstructor
@Getter
public enum ConfigurationPropertyKey implements TypedPropertyKey {
    
    /**
     * Whether show SQL in log.
     */
    SQL_SHOW("sql-show", String.valueOf(Boolean.FALSE), boolean.class),
    
    /**
     * Whether show SQL details in simple style.
     */
    SQL_SIMPLE("sql-simple", String.valueOf(Boolean.FALSE), boolean.class),
    
    /**
     * The max thread size of worker group to execute SQL.
     */
    EXECUTOR_SIZE("executor-size", String.valueOf(0), int.class),
    
    /**
     * Max opened connection size for each query.
     */
    MAX_CONNECTIONS_SIZE_PER_QUERY("max-connections-size-per-query", String.valueOf(1), int.class),
    
    /**
     * Whether validate table meta data consistency when application startup or updated.
     */
    CHECK_TABLE_METADATA_ENABLED("check-table-metadata-enabled", String.valueOf(Boolean.FALSE), boolean.class),
    
    /**
     * Frontend database protocol type for ShardingSphere-Proxy.
     */
    PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE("proxy-frontend-database-protocol-type", "", String.class),
    
    /**
     * Flush threshold for every records from databases for ShardingSphere-Proxy.
     */
    PROXY_FRONTEND_FLUSH_THRESHOLD("proxy-frontend-flush-threshold", String.valueOf(128), int.class),
    
    /**
     * Whether enable opentracing for ShardingSphere-Proxy.
     */
    PROXY_OPENTRACING_ENABLED("proxy-opentracing-enabled", String.valueOf(Boolean.FALSE), boolean.class),
    
    /**
     * Whether enable hint for ShardingSphere-Proxy.
     */
    PROXY_HINT_ENABLED("proxy-hint-enabled", String.valueOf(Boolean.FALSE), boolean.class),
    
    /**
     * Whether enable show process list.
     */
    SHOW_PROCESS_LIST_ENABLED("show-process-list-enabled", String.valueOf(Boolean.FALSE), boolean.class),
    
    /**
     * The length of time in milliseconds an SQL waits for a global lock before giving up.
     */
    LOCK_WAIT_TIMEOUT_MILLISECONDS("lock-wait-timeout-milliseconds", String.valueOf(50000L), long.class),
    
    /**
     * Whether enable lock.
     */
    LOCK_ENABLED("lock-enabled", String.valueOf(Boolean.FALSE), boolean.class),
    
    /**
     * Proxy backend query fetch size. A larger value may increase the memory usage of ShardingSphere Proxy.
     * The default value is -1, which means set the minimum value for different JDBC drivers.
     */
    PROXY_BACKEND_QUERY_FETCH_SIZE("proxy-backend-query-fetch-size", String.valueOf(-1), int.class),
    
    /**
     * Whether check duplicate table.
     */
    CHECK_DUPLICATE_TABLE_ENABLED("check-duplicate-table-enabled", String.valueOf(Boolean.FALSE), boolean.class),
    
    /**
     * Whether enable SQL comment parse.
     */
    SQL_COMMENT_PARSE_ENABLED("sql-comment-parse-enabled", String.valueOf(Boolean.FALSE), boolean.class),
    
    /**
     * Proxy frontend executor size. The default value is 0, which means let Netty decide.
     */
    PROXY_FRONTEND_EXECUTOR_SIZE("proxy-frontend-executor-size", String.valueOf(0), int.class),
    
    /**
     * Available options of proxy backend executor suitable: OLAP(default), OLTP. The OLTP option may reduce time cost of writing packets to client, but it may increase the latency of SQL execution
     * if client connections are more than proxy-frontend-netty-executor-size, especially executing slow SQL.
     */
    PROXY_BACKEND_EXECUTOR_SUITABLE("proxy-backend-executor-suitable", "OLAP", String.class);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
}
