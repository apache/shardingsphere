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

package org.apache.shardingsphere.mode.manager.cluster.coordinator;

import lombok.Getter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.utils.IpUtils;

import java.util.UUID;

/**
 * Cluster instance.
 */
@Getter
public final class ClusterInstance {
    
    private static final String DELIMITER = "@";
    
    private static final ClusterInstance INSTANCE = new ClusterInstance();
    
    private String id;
    
    private ClusterInstance() {
    }
    
    /**
     * Init cluster instance.
     * 
     * @param port port
     */
    public void init(final Integer port) {
        id = null == port ? String.join(DELIMITER, IpUtils.getIp(), UUID.randomUUID().toString()) 
                : String.join(DELIMITER, IpUtils.getIp(), String.valueOf(port), UUID.randomUUID().toString());
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static ClusterInstance getInstance() {
        return INSTANCE;
    }
}
