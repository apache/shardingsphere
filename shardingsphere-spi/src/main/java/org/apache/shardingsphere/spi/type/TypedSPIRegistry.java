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

package org.apache.shardingsphere.spi.type;

import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.exception.ServiceProviderNotFoundException;

import java.util.Optional;
import java.util.Properties;

/**
 * Typed SPI registry.
 */
public final class TypedSPIRegistry {
    
    /**
     * Get registered service.
     * 
     * @param typedClass typed class
     * @param type type
     * @param props properties
     * @param <T> type
     * @return registered service
     */
    public static <T extends TypeBasedSPI> T getRegisteredService(final Class<T> typedClass, final String type, final Properties props) {
        Optional<T> serviceInstance = ShardingSphereServiceLoader.newServiceInstances(typedClass).stream().filter(each -> type.equalsIgnoreCase(each.getType())).findFirst();
        if (serviceInstance.isPresent()) {
            T result = serviceInstance.get();
            result.setProperties(props);
            return result;
        }
        throw new ServiceProviderNotFoundException(typedClass, type);
    }
    
    /**
     * Get registered service.
     *
     * @param typedClass typed class
     * @param <T> type
     * @return registered service
     */
    public static <T extends TypeBasedSPI> T getRegisteredService(final Class<T> typedClass) {
        Optional<T> serviceInstance = ShardingSphereServiceLoader.newServiceInstances(typedClass).stream().findFirst();
        if (serviceInstance.isPresent()) {
            return serviceInstance.get();
        }
        throw new ServiceProviderNotFoundException(typedClass);
    }
}
