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

package org.apache.shardingsphere.infra.spi.type.typed;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;

import java.util.Optional;
import java.util.Properties;

/**
 * Typed SPI loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TypedSPILoader {
    
    /**
     * Find service.
     * 
     * @param serviceInterface typed SPI service interface
     * @param type type
     * @param <T> SPI class type
     * @return found service
     */
    public static <T extends TypedSPI> Optional<T> findService(final Class<T> serviceInterface, final Object type) {
        return findService(serviceInterface, type, new Properties());
    }
    
    /**
     * Find service.
     * 
     * @param serviceInterface typed SPI service interface
     * @param type type
     * @param props properties
     * @param <T> SPI class type
     * @return found service
     */
    public static <T extends TypedSPI> Optional<T> findService(final Class<T> serviceInterface, final Object type, final Properties props) {
        if (null == type) {
            return findDefaultService(serviceInterface);
        }
        for (T each : ShardingSphereServiceLoader.getServiceInstances(serviceInterface)) {
            if (matchesType(type, each)) {
                each.init(null == props ? new Properties() : convertToStringTypedProperties(props));
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private static <T extends TypedSPI> Optional<T> findDefaultService(final Class<T> serviceInterface) {
        for (T each : ShardingSphereServiceLoader.getServiceInstances(serviceInterface)) {
            if (!each.isDefault()) {
                continue;
            }
            each.init(new Properties());
            return Optional.of(each);
        }
        return Optional.empty();
    }
    
    private static Properties convertToStringTypedProperties(final Properties props) {
        if (props.isEmpty()) {
            return props;
        }
        Properties result = new Properties();
        props.forEach((key, value) -> result.setProperty(key.toString(), null == value ? null : value.toString()));
        return result;
    }
    
    /**
     * Get service.
     * 
     * @param serviceInterface typed SPI service interface
     * @param type type
     * @param <T> SPI class type
     * @return service
     */
    public static <T extends TypedSPI> T getService(final Class<T> serviceInterface, final Object type) {
        return getService(serviceInterface, type, new Properties());
    }
    
    /**
     * Get service.
     * 
     * @param serviceInterface typed SPI service interface
     * @param type type
     * @param props properties
     * @param <T> SPI class type
     * @return service
     */
    public static <T extends TypedSPI> T getService(final Class<T> serviceInterface, final Object type, final Properties props) {
        return findService(serviceInterface, type, props).orElseThrow(() -> new ServiceProviderNotFoundException(serviceInterface, type));
    }
    
    /**
     * Check service.
     * 
     * @param serviceInterface typed SPI service interface
     * @param type type
     * @param props properties
     * @param <T> SPI class type
     * @throws ServiceProviderNotFoundException service provider not found server exception
     */
    public static <T extends TypedSPI> void checkService(final Class<T> serviceInterface, final Object type, final Properties props) {
        for (T each : ShardingSphereServiceLoader.getServiceInstances(serviceInterface)) {
            if (matchesType(type, each)) {
                each.init(null == props ? new Properties() : convertToStringTypedProperties(props));
                return;
            }
        }
        throw new ServiceProviderNotFoundException(serviceInterface, type);
    }
    
    private static boolean matchesType(final Object type, final TypedSPI instance) {
        Object instanceType = instance.getType();
        if (null == instanceType) {
            return false;
        }
        if (instanceType instanceof String && type instanceof String) {
            return instanceType.toString().equalsIgnoreCase(type.toString()) || instance.getTypeAliases().contains(type);
        }
        return instanceType.equals(type) || instance.getTypeAliases().contains(type);
    }
}
