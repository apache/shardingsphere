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

package org.apache.shardingsphere.core.constant.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Sharding properties constant.
 * 
 * @author gaohongtao
 * @author caohao
 * @author cookie
 */
@RequiredArgsConstructor
@Getter
public enum ShardingPropertiesConstant {
    
    /**
     * Enable or Disable to show SQL details.
     * 
     * <p>
     * Print SQL details can help developers debug easier. 
     * The details includes: logic SQL, parse context and rewrote actual SQL list. 
     * Enable this property will log into log topic: {@code ShardingSphere-SQL}, log level is {@code INFO}.
     * Default: false
     * </p>
     */
    SQL_SHOW("sql.show", String.valueOf(Boolean.FALSE), boolean.class),

    /**
     *<p>
     * Maximum length of logic SQL.
     * In sharding mode, if the length of logic SQL longer than the number, log will display in simple style to avoid too much content .
     * Default: Integer.MAX_VALUE
     *</p>
     */
    SQL_SIMPLE_LENGTH("sql.simple.length", String.valueOf(Integer.MAX_VALUE), int.class),

    /**
     * Worker group or user group thread max size.
     *
     * <p>
     * Worker group accept tcp connection.
     * Command execute engine accept MySQL command.
     * Default: CPU cores * 2.
     * </p>
     */
    ACCEPTOR_SIZE("acceptor.size", String.valueOf(Runtime.getRuntime().availableProcessors() * 2), int.class),
    
    /**
     * Worker thread max size.
     * 
     * <p>
     * Execute SQL Statement and PrepareStatement will use this thread pool.
     * One sharding data source will use a independent thread pool, it does not share thread pool even different data source in same JVM.
     * Default: infinite.
     * </p>
     */
    EXECUTOR_SIZE("executor.size", String.valueOf(0), int.class),
    
    /**
     * Max opened connection size for each query.
     */
    MAX_CONNECTIONS_SIZE_PER_QUERY("max.connections.size.per.query", String.valueOf(1), int.class),
    
    /**
     * Sharding-Proxy's flush threshold for every records from databases.
     */
    PROXY_FRONTEND_FLUSH_THRESHOLD("proxy.frontend.flush.threshold", String.valueOf(128), int.class),
    
    /**
     * Transaction type of proxy.
     *
     * <p>
     * LOCAL:
     * Sharding-Proxy will run with LOCAL transaction.
     * </p>
     *
     * <p>
     * XA:
     * Sharding-Proxy will run with XA transaction.
     * </p>
     *
     * <p>
     * BASE:
     * Sharding-Proxy will run with BASE transaction.
     * </p>
     */
    PROXY_TRANSACTION_TYPE("proxy.transaction.type", "LOCAL", String.class),
    
    /**
     * Enable opentracing for Sharding-Proxy.
     */
    PROXY_OPENTRACING_ENABLED("proxy.opentracing.enabled", String.valueOf(Boolean.FALSE), boolean.class),
    
    PROXY_BACKEND_MAX_CONNECTIONS("proxy.backend.max.connections", String.valueOf(8), int.class),
    
    PROXY_BACKEND_CONNECTION_TIMEOUT_SECONDS("proxy.backend.connection.timeout.seconds", String.valueOf(60), int.class),
    
    CHECK_TABLE_METADATA_ENABLED("check.table.metadata.enabled", String.valueOf(Boolean.FALSE), boolean.class);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
    
    /**
     * Find value via property key.
     * 
     * @param key property key
     * @return value enum, return {@code null} if not found
     */
    public static ShardingPropertiesConstant findByKey(final String key) {
        for (ShardingPropertiesConstant each : ShardingPropertiesConstant.values()) {
            if (each.getKey().equals(key)) {
                return each;
            }
        }
        return null;
    }
}
