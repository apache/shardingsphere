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

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service controller.
 */
public final class ServiceController {
    
    private static final String PERSISTENT_SERVICE_NAME = "PERSISTENT";
    
    private static final String EPHEMERAL_SERVICE_NAME = "EPHEMERAL";
    
    @Getter
    private final ServiceMetadata persistentService = new ServiceMetadata(PERSISTENT_SERVICE_NAME, false);
    
    @Getter
    private final ServiceMetadata ephemeralService = new ServiceMetadata(EPHEMERAL_SERVICE_NAME, true);
    
    private final Map<Boolean, ServiceMetadata> serviceMap = Stream.of(persistentService, ephemeralService).collect(Collectors.toMap(ServiceMetadata::isEphemeral, Function.identity()));
    
    /**
     * Get all services.
     * 
     * @return all services
     */
    public Collection<ServiceMetadata> getAllServices() {
        return serviceMap.values();
    }
    
    /**
     * Get service.
     * 
     * @param ephemeral is ephemeral service
     * @return ephemeral service or persistent service
     */
    public ServiceMetadata getService(final boolean ephemeral) {
        return serviceMap.get(ephemeral);
    }
}
