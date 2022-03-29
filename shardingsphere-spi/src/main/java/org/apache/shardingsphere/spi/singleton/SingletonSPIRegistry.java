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

package org.apache.shardingsphere.spi.singleton;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPI;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Singleton SPI registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SingletonSPIRegistry {
    
    /**
     * Get singleton instances map.
     *
     * @param singletonSPIClass singleton SPI class
     * @param keyMapper key mapper
     * @param <K> the output type of the key mapping function
     * @param <T> the type of the input elements
     * @return singleton instances map
     */
    public static <K, T extends SingletonSPI> Map<K, T> getSingletonInstancesMap(final Class<T> singletonSPIClass, final Function<? super T, ? extends K> keyMapper) {
        ShardingSphereServiceLoader.register(singletonSPIClass);
        Collection<T> instances = ShardingSphereServiceLoader.getSingletonServiceInstances(singletonSPIClass);
        return instances.stream().collect(Collectors.toMap(keyMapper, Function.identity()));
    }
    
    /**
     * Get typed singleton instances map.
     * <p>
     *     Notice: Map key is {@linkplain TypedSPI#getType()}, it won't be converted to upper case or lower case. If type is case-insensitive, then try {@linkplain TypedSingletonSPIHolder}.
     * </p>
     *
     * @param singletonSPIClass singleton SPI class
     * @param <T> the type of the input elements
     * @return singleton instances map
     */
    public static <T extends TypedSPI & SingletonSPI> Map<String, T> getTypedSingletonInstancesMap(final Class<T> singletonSPIClass) {
        return getSingletonInstancesMap(singletonSPIClass, TypedSPI::getType);
    }
}
