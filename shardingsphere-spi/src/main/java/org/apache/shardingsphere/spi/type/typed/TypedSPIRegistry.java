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

package org.apache.shardingsphere.spi.type.typed;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.exception.ServiceProviderNotFoundException;

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
     * @param spiClass stateless typed SPI class
     * @param type type
     * @param <T> SPI class type
     * @return registered service
     */
    public static <T extends StatelessTypedSPI> Optional<T> findRegisteredService(final Class<T> spiClass, final String type) {
        for (T each : ShardingSphereServiceLoader.getSingletonServiceInstances(spiClass)) {
            if (matchesType(type, each)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Find registered service.
     *
     * @param spiClass stateful typed SPI class
     * @param type type
     * @param props properties
     * @param <T> SPI class type
     * @return registered service
     */
    public static <T extends StatefulTypedSPI> Optional<T> findRegisteredService(final Class<T> spiClass, final String type, final Properties props) {
        for (T each : ShardingSphereServiceLoader.newServiceInstances(spiClass)) {
            if (matchesType(type, each)) {
                setProperties(each, props);
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private static boolean matchesType(final String type, final TypedSPI typedSPI) {
        return typedSPI.getType().equalsIgnoreCase(type) || typedSPI.getTypeAliases().contains(type);
    }
    
    private static <T extends StatefulTypedSPI> void setProperties(final T statefulTypedSPI, final Properties props) {
        if (null == props) {
            return;
        }
        Properties newProps = new Properties();
        props.forEach((key, value) -> newProps.setProperty(key.toString(), null == value ? null : value.toString()));
        statefulTypedSPI.setProps(newProps);
    }
    
    /**
     * Get registered service.
     *
     * @param spiClass stateless typed SPI class
     * @param type type
     * @param <T> SPI class type
     * @return registered service
     */
    public static <T extends StatelessTypedSPI> T getRegisteredService(final Class<T> spiClass, final String type) {
        Optional<T> result = findRegisteredService(spiClass, type);
        if (result.isPresent()) {
            return result.get();
        }
        throw new ServiceProviderNotFoundException(spiClass, type);
    }
    
    /**
     * Get registered service.
     * 
     * @param spiClass stateful typed SPI class
     * @param type type
     * @param props properties
     * @param <T> SPI class type
     * @return registered service
     */
    public static <T extends StatefulTypedSPI> T getRegisteredService(final Class<T> spiClass, final String type, final Properties props) {
        Optional<T> result = findRegisteredService(spiClass, type, props);
        if (result.isPresent()) {
            return result.get();
        }
        throw new ServiceProviderNotFoundException(spiClass, type);
    }
}
