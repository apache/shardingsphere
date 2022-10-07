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

package org.apache.shardingsphere.agent.core.spi;

import java.util.Collection;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.core.exception.AgentServiceProviderNotFoundException;
import org.apache.shardingsphere.agent.spi.type.AgentTypedSPI;

/**
 *  Agent typed SPI registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AgentTypedSPIRegistry {
    
    /**
     * Get registered service.
     * 
     * @param typedSPIClass typed SPI class
     * @param type type
     * @param <T> type
     * @return registered service
     */
    public static <T extends AgentTypedSPI> T getRegisteredService(final Class<T> typedSPIClass, final String type) {
        Optional<T> serviceInstance = AgentServiceLoader.getServiceLoader(typedSPIClass).newServiceInstances().stream().filter(each -> each.getType().equalsIgnoreCase(type)).findFirst();
        if (serviceInstance.isPresent()) {
            return serviceInstance.get();
        }
        throw new AgentServiceProviderNotFoundException(typedSPIClass, type);
    }
    
    /**
     * Get registered service.
     *
     * @param typedSPIClass typed SPI class
     * @param type type
     * @param <T> type
     * @return registered service
     */
    public static <T extends AgentTypedSPI> Optional<T> getRegisteredServiceOptional(final Class<T> typedSPIClass, final String type) {
        return AgentServiceLoader.getServiceLoader(typedSPIClass).newServiceInstances().stream().filter(each -> each.getType().equalsIgnoreCase(type)).findFirst();
    }
    
    /**
     * Get all registered service.
     *
     * @param typedSPIClass typed SPI class
     * @param <T> type
     * @return registered service
     */
    public static <T extends AgentTypedSPI> Collection<T> getAllRegisteredService(final Class<T> typedSPIClass) {
        return AgentServiceLoader.getServiceLoader(typedSPIClass).newServiceInstances();
    }
}
