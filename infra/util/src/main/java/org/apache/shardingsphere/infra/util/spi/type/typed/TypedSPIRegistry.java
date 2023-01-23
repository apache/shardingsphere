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

package org.apache.shardingsphere.infra.util.spi.type.typed;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.exception.ServiceProviderNotFoundServerException;
import org.apache.shardingsphere.infra.util.spi.lifecycle.SPIPostProcessor;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPI;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPIRegistry;

import java.util.Optional;
import java.util.Properties;

/**
 * Typed SPI registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TypedSPIRegistry {
    
    /**
     * Judge whether contains service.
     * 
     * @param spiClass typed SPI class
     * @param type type
     * @param <T> SPI class type
     * @return contains or not
     */
    public static <T extends TypedSPI> boolean contains(final Class<T> spiClass, final String type) {
        return ShardingSphereServiceLoader.getServiceInstances(spiClass).stream().anyMatch(each -> matchesType(type, each));
    }
    
    /**
     * Find service.
     *
     * @param spiClass typed SPI class
     * @param type type
     * @param <T> SPI class type
     * @return service
     */
    public static <T extends TypedSPI> Optional<T> findService(final Class<T> spiClass, final String type) {
        for (T each : ShardingSphereServiceLoader.getServiceInstances(spiClass)) {
            if (matchesType(type, each)) {
                return Optional.of(each);
            }
        }
        return getRequiredSPI(spiClass);
    }
    
    /**
     * Find service.
     *
     * @param spiClass typed SPI class
     * @param type type
     * @param props properties
     * @param <T> SPI class type
     * @return service
     */
    public static <T extends TypedSPI> Optional<T> findService(final Class<T> spiClass, final String type, final Properties props) {
        for (T each : ShardingSphereServiceLoader.getServiceInstances(spiClass)) {
            if (matchesType(type, each)) {
                Properties stringTypeProps = convertToStringTypedProperties(props);
                if (each instanceof SPIPostProcessor) {
                    ((SPIPostProcessor) each).init(stringTypeProps);
                }
                return Optional.of(each);
            }
        }
        return getRequiredSPI(spiClass);
    }
    
    private static boolean matchesType(final String type, final TypedSPI instance) {
        return instance.getType().equalsIgnoreCase(type) || instance.getTypeAliases().contains(type);
    }
    
    private static Properties convertToStringTypedProperties(final Properties props) {
        if (null == props) {
            return new Properties();
        }
        Properties result = new Properties();
        props.forEach((key, value) -> result.setProperty(key.toString(), null == value ? null : value.toString()));
        return result;
    }
    
    /**
     * Get service.
     *
     * @param spiClass typed SPI class
     * @param type type
     * @param <T> SPI class type
     * @return service
     */
    public static <T extends TypedSPI> T getService(final Class<T> spiClass, final String type) {
        return findService(spiClass, type).orElseGet(() -> getRequiredSPI(spiClass).orElseThrow(() -> new ServiceProviderNotFoundServerException(spiClass, type)));
    }
    
    /**
     * Get service.
     * 
     * @param spiClass typed SPI class
     * @param type type
     * @param props properties
     * @param <T> SPI class type
     * @return service
     */
    public static <T extends TypedSPI> T getService(final Class<T> spiClass, final String type, final Properties props) {
        return findService(spiClass, type, props).orElseGet(() -> getRequiredSPI(spiClass).orElseThrow(() -> new ServiceProviderNotFoundServerException(spiClass, type)));
    }
    
    @SuppressWarnings("unchecked")
    private static <T extends TypedSPI> Optional<T> getRequiredSPI(final Class<T> spiClass) {
        return RequiredSPI.class.isAssignableFrom(spiClass) ? Optional.of((T) RequiredSPIRegistry.getService((Class<? extends RequiredSPI>) spiClass)) : Optional.empty();
    }
}
