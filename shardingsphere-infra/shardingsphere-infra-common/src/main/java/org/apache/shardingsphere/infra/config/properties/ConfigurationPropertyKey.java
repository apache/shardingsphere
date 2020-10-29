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
     * The max thread size of accepter group to accept TCP connections.
     */
    ACCEPTOR_SIZE("acceptor-size", String.valueOf(Runtime.getRuntime().availableProcessors() * 2), int.class),
    
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
     * Whether query with cipher column for data encrypt.
     */
    QUERY_WITH_CIPHER_COLUMN("query-with-cipher-column", String.valueOf(Boolean.TRUE), boolean.class),
    
    /**
     * Flush threshold for every records from databases for ShardingSphere-Proxy.
     */
    PROXY_FRONTEND_FLUSH_THRESHOLD("proxy-frontend-flush-threshold", String.valueOf(128), int.class),
    
    /**
     * Transaction type of proxy.
     *
     * <p>
     * LOCAL:
     * ShardingSphere-Proxy will run with LOCAL transaction.
     * </p>
     *
     * <p>
     * XA:
     * ShardingSphere-Proxy will run with XA transaction.
     * </p>
     *
     * <p>
     * BASE:
     * ShardingSphere-Proxy will run with BASE transaction.
     * </p>
     */
    PROXY_TRANSACTION_TYPE("proxy-transaction-type", "LOCAL", String.class),
    
    /**
     * Whether enable opentracing for ShardingSphere-Proxy.
     */
    PROXY_OPENTRACING_ENABLED("proxy-opentracing-enabled", String.valueOf(Boolean.FALSE), boolean.class),
    
    /**
     * Whether enable hint for ShardingSphere-Proxy.
     */
    PROXY_HINT_ENABLED("proxy-hint-enabled", String.valueOf(Boolean.FALSE), boolean.class);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
}
