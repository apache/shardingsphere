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

package org.apache.shardingsphere.infra.spi.typed;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;

import java.util.Optional;
import java.util.Properties;

/**
 * Typed SPI registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TypedSPIRegistry {
    
    /**
     * Get registered service.
     * 
     * @param typedSPIClass typed SPI class
     * @param type type
     * @param props properties
     * @param <T> type
     * @return registered service
     */
    public static <T extends TypedSPI> T getRegisteredService(final Class<T> typedSPIClass, final String type, final Properties props) {
        Optional<T> serviceInstance = ShardingSphereServiceLoader.newServiceInstances(typedSPIClass).stream().filter(each -> each.getType().equalsIgnoreCase(type)).findFirst();
        if (serviceInstance.isPresent()) {
            T result = serviceInstance.get();
            convertPropertiesValueType(props, result);
            return result;
        }
        throw new ServiceProviderNotFoundException(typedSPIClass, type);
    }
    
    /**
     * Get registered service.
     *
     * @param typedSPIClass typed SPI class
     * @param <T> type
     * @return registered service
     */
    public static <T extends TypedSPI> T getRegisteredService(final Class<T> typedSPIClass) {
        Optional<T> serviceInstance = ShardingSphereServiceLoader.newServiceInstances(typedSPIClass).stream().findFirst();
        if (serviceInstance.isPresent()) {
            return serviceInstance.get();
        }
        throw new ServiceProviderNotFoundException(typedSPIClass);
    }
    
    private static <T extends TypedSPI> void convertPropertiesValueType(final Properties props, final T service) {
        if (null != props) {
            Properties newProps = new Properties();
            props.forEach((key, value) -> newProps.setProperty(key.toString(), null == value ? null : value.toString()));
            service.setProps(newProps);
        }
    }
}
