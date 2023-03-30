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

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.mode.repository.cluster.nacos.listener.NamingEventListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service meta data.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class ServiceMetaData {
    
    private final String serviceName;
    
    private String ip;
    
    private AtomicInteger port;
    
    private NamingEventListener listener;
    
    private final boolean ephemeral;
    
    /**
     * Get incremental port.
     * 
     * @return incremental port
     */
    public int getPort() {
        int result = port.incrementAndGet();
        Preconditions.checkState(Integer.MIN_VALUE != result, "Specified cluster ip exceeded the maximum number of persisting");
        return result;
    }
}
