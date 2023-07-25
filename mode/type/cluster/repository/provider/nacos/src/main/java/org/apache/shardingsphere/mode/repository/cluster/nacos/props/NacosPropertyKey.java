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
    CLUSTER_IP("clusterIp", "", String.class),
    
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
    TIME_TO_LIVE_SECONDS("timeToLiveSeconds", String.valueOf(30), int.class),
    
    /**
     * Username.
     */
    USERNAME("username", "", String.class),
    
    /**
     * Password.
     */
    PASSWORD("password", "", String.class);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
}
