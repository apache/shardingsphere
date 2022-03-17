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

package org.apache.shardingsphere.spi.typed;

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
     * @param typedSPIClass typed SPI class
     * @param type type
     * @param props properties
     * @param <T> type
     * @return registered service
     */
    public static <T extends TypedSPI> Optional<T> findRegisteredService(final Class<T> typedSPIClass, final String type, final Properties props) {
        for (T each : ShardingSphereServiceLoader.newServiceInstances(typedSPIClass)) {
            if (each.getType().equalsIgnoreCase(type) || each.getTypeAliases().contains(type)) {
                setProperties(each, props);
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private static <T extends TypedSPI> void setProperties(final T service, final Properties props) {
        if (null == props) {
            return;
        }
        Properties newProps = new Properties();
        props.forEach((key, value) -> newProps.setProperty(key.toString(), null == value ? null : value.toString()));
        service.setProps(newProps);
    }
    
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
        Optional<T> result = findRegisteredService(typedSPIClass, type, props);
        if (result.isPresent()) {
            return result.get();
        }
        throw new ServiceProviderNotFoundException(typedSPIClass, type);
    }
}
