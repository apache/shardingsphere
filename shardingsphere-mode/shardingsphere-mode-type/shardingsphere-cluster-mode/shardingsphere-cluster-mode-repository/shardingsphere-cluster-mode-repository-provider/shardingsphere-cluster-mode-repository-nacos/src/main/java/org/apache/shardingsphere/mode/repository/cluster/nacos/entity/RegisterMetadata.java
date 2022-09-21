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

package org.apache.shardingsphere.mode.repository.cluster.nacos.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.repository.cluster.nacos.listener.NamingEventListener;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Register metadata.
 */
@RequiredArgsConstructor
@Getter
public enum RegisterMetadata {
    
    /**
     * Persistent service.
     */
    PERSISTENT(new AtomicInteger(Integer.MIN_VALUE), new NamingEventListener(), false),
    
    /**
     * Ephemeral service.
     */
    EPHEMERAL(new AtomicInteger(Integer.MIN_VALUE), new NamingEventListener(), true);
    
    private final AtomicInteger port;
    
    private final NamingEventListener listener;
    
    private final boolean ephemeral;
    
    /**
     * Find corresponding registerMetadata.
     *
     * @param isEphemeral isEphemeral
     * @return registerMetadata
     */
    public static RegisterMetadata of(final boolean isEphemeral) {
        return Stream.of(values())
                .filter(registerMetadata -> registerMetadata.ephemeral == isEphemeral).findAny()
                .orElseThrow(() -> new IllegalArgumentException("Status not exist: " + isEphemeral));
    }
}
