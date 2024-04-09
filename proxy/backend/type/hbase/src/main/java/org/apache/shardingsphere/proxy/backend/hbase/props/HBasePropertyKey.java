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

package org.apache.shardingsphere.proxy.backend.hbase.props;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.props.TypedPropertyKey;

/**
 * Typed property key of HBase.
 */
@RequiredArgsConstructor
@Getter
public enum HBasePropertyKey implements TypedPropertyKey {
    
    /**
     * Background threads are periodically executed to update metadata.
     */
    META_REFRESHER_INTERVAL_SECONDS("meta-refresher-interval-seconds", String.valueOf(300), int.class),
    
    /**
     * Maximum number of scanned rows.
     */
    MAX_SCAN_LIMIT_SIZE("max-scan-limit-size", String.valueOf(5000), long.class),
    
    /**
     * Warm up thread num.
     */
    WARM_UP_THREAD_NUM("warm-up-thread-num", String.valueOf(1), int.class),
    
    /**
     * Is sync warm up.
     */
    IS_SYNC_WARM_UP("is-sync-warm-up", String.valueOf(true), boolean.class),
    
    /**
     * HBase execute out time.
     */
    EXECUTE_TIME_OUT("execute-time-out", String.valueOf(2000), Long.class);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
}
