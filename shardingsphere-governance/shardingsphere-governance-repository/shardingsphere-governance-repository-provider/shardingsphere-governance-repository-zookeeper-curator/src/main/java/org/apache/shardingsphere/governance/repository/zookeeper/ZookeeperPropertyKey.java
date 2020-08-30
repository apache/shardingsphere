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

package org.apache.shardingsphere.governance.repository.zookeeper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.properties.TypedPropertyKey;

/**
 * Typed property key of Zookeeper.
 */
@RequiredArgsConstructor
@Getter
public enum ZookeeperPropertyKey implements TypedPropertyKey {
    
    /**
     * Retry interval milliseconds when connect with ZooKeeper curator client.
     */
    RETRY_INTERVAL_MILLISECONDS("retryIntervalMilliseconds", String.valueOf(500), int.class),
    
    /**
     * Max Retry times when connect with ZooKeeper curator client.
     */
    MAX_RETRIES("maxRetries", String.valueOf(3), int.class),
    
    /**
     * ZooKeeper client session timeout value.
     */
    TIME_TO_LIVE_SECONDS("timeToLiveSeconds", String.valueOf(60), int.class),
    
    /**
     * ZooKeeper client operation timeout value.
     */
    OPERATION_TIMEOUT_MILLISECONDS("operationTimeoutMilliseconds", String.valueOf(500), int.class),
    
    /**
     * ZooKeeper client connection authorization schema name.
     */
    DIGEST("digest", "", String.class);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
}
