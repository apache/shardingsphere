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

package org.apache.shardingsphere.infra.util.spi.type.optional;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;

import java.util.Collection;
import java.util.Optional;

/**
 * Optional SPI registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OptionalSPIRegistry {
    
    /**
     * Find registered service.
     *
     * @param spiClass optional SPI class
     * @param <T> SPI class type
     * @return registered service
     */
    public static <T extends OptionalSPI> Optional<T> findRegisteredService(final Class<T> spiClass) {
        Collection<T> result = ShardingSphereServiceLoader.getServiceInstances(spiClass);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.iterator().next());
    }
}
