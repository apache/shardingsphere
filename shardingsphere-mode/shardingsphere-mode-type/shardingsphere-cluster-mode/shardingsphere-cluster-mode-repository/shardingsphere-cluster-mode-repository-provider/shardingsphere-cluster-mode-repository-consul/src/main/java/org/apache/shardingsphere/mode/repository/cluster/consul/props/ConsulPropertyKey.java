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

package org.apache.shardingsphere.mode.repository.cluster.consul.props;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.props.TypedPropertyKey;

/**
 * Typed property key of Consul.
 */
@RequiredArgsConstructor
@Getter
public enum ConsulPropertyKey implements TypedPropertyKey {
    
    /**
     * Time to live in seconds.
     */
    TIME_TO_LIVE_IN_SECONDS("timeToLiveInSeconds", "30", long.class),
    
    /**
     * Block query time in seconds.
     */
    BLOCK_QUERY_TIME_IN_SECONDS("blockQueryTimeInSeconds", "60", long.class);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
}
