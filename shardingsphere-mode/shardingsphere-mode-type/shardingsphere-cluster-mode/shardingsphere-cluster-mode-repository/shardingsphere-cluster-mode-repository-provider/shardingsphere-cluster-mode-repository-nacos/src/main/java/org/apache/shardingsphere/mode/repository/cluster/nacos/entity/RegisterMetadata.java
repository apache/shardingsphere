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
import lombok.Setter;
import org.apache.shardingsphere.mode.repository.cluster.nacos.listener.NamingEventListener;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Register metadata.
 */
@RequiredArgsConstructor
public enum RegisterMetadata {
    
    /**
     * persistent.
     */
    PERSISTENT(false),
    
    /**
     * ephemeral.
     */
    EPHEMERAL(true);
    
    @Setter
    private AtomicInteger port;
    
    @Setter
    @Getter
    private NamingEventListener listener;
    
    @Getter
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
    
    /**
     * This is to ensure that port is different from the last time when persisting.
     *
     * @return fake port
     */
    public int getPort() {
        int port = this.port.incrementAndGet();
        if (port == Integer.MIN_VALUE) {
            throw new IllegalStateException("Specified cluster ip exceeded the maximum number of persisting");
        }
        return port;
    }
}
