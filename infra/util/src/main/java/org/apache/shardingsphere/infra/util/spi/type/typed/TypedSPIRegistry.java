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

import java.util.Optional;
import java.util.Properties;

/**
 * Typed SPI registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TypedSPIRegistry {
    
    /**
     * Find registered service.
     *
     * @param spiClass typed SPI class
     * @param type type
     * @param <T> SPI class type
     * @return registered service
     */
    public static <T extends TypedSPI> Optional<T> findRegisteredService(final Class<T> spiClass, final String type) {
        for (T each : ShardingSphereServiceLoader.getServiceInstances(spiClass)) {
            if (matchesType(type, each)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Find registered service.
     *
     * @param spiClass typed SPI class
     * @param type type
     * @param props properties
     * @param <T> SPI class type
     * @return registered service
     */
    public static <T extends TypedSPI> Optional<T> findRegisteredService(final Class<T> spiClass, final String type, final Properties props) {
        for (T each : ShardingSphereServiceLoader.getServiceInstances(spiClass)) {
            if (matchesType(type, each)) {
                Properties stringTypeProps = convertToStringTypedProperties(props);
                if (each instanceof SPIPostProcessor) {
                    ((SPIPostProcessor) each).init(stringTypeProps);
                }
                return Optional.of(each);
            }
        }
        return Optional.empty();
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
     * Get registered service.
     *
     * @param spiClass typed SPI class
     * @param type type
     * @param <T> SPI class type
     * @return registered service
     */
    public static <T extends TypedSPI> T getRegisteredService(final Class<T> spiClass, final String type) {
        Optional<T> result = findRegisteredService(spiClass, type);
        if (result.isPresent()) {
            return result.get();
        }
        throw new ServiceProviderNotFoundServerException(spiClass, type);
    }
    
    /**
     * Get registered service.
     * 
     * @param spiClass typed SPI class
     * @param type type
     * @param props properties
     * @param <T> SPI class type
     * @return registered service
     */
    public static <T extends TypedSPI> T getRegisteredService(final Class<T> spiClass, final String type, final Properties props) {
        Optional<T> result = findRegisteredService(spiClass, type, props);
        if (result.isPresent()) {
            return result.get();
        }
        throw new ServiceProviderNotFoundServerException(spiClass, type);
    }
}
