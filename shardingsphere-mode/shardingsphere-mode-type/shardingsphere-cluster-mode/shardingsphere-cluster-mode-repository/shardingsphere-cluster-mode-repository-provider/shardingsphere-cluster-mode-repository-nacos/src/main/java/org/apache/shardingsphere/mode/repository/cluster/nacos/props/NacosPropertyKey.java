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

package org.apache.shardingsphere.mode.repository.cluster.nacos.props;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.instance.utils.IpUtils;
import org.apache.shardingsphere.infra.util.props.TypedPropertyKey;

/**
 * Typed property key of Nacos.
 */
@RequiredArgsConstructor
@Getter
public enum NacosPropertyKey implements TypedPropertyKey {
    
    /**
     * Cluster ip.
     */
    CLUSTER_IP("clusterIp", IpUtils.getIp(), String.class),
    
    /**
     * Url of dataSource.
     */
    DATA_SOURCE_POOL_CLASS_NAME("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource", String.class),
    
    /**
     * Url of dataSource.
     */
    URL("url", "", String.class),
    
    /**
     * Username of dataSource.
     */
    USERNAME("username", "root", String.class),
    
    /**
     * Password of dataSource.
     */
    PASSWORD("password", "", String.class),
    
    /**
     * ConnectionTimeoutMilliseconds of pool.
     */
    CONNECTION_TIMEOUT_MILLISECONDS("connectionTimeoutMilliseconds", String.valueOf(30000), long.class),
    
    /**
     * IdleTimeoutMilliseconds of pool.
     */
    IDLE_TIMEOUT_MILLISECONDS("idleTimeoutMilliseconds", String.valueOf(60000), long.class),
    
    /**
     * MaxLifetimeMilliseconds of pool.
     */
    MAX_LIFETIME_MILLISECONDS("maxLifetimeMilliseconds", String.valueOf(1800000), long.class),
    
    /**
     * MaxPoolSize of pool.
     */
    MAX_POOL_SIZE("maxPoolSize", String.valueOf(50), int.class),
    
    /**
     * MinPoolSize of pool.
     */
    MIN_POOL_SIZE("minPoolSize", String.valueOf(1), int.class),
    
    /**
     * Init distributed lock schema.
     */
    INIT_SCHEMA("initSchema", String.valueOf(false), boolean.class),
    
    /**
     * Retry interval milliseconds when checking whether value is available.
     */
    RETRY_INTERVAL_MILLISECONDS("retryIntervalMilliseconds", String.valueOf(500), long.class),
    
    /**
     * Max Retry times when checking whether value is available.
     */
    MAX_RETRIES("maxRetries", String.valueOf(3), int.class),
    
    /**
     * Time to live seconds.
     */
    TIME_TO_LIVE_SECONDS("timeToLiveSeconds", String.valueOf(30), int.class);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
}
