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

package org.apache.shardingsphere.infra.util.spi.type.required;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.util.spi.lifecycle.SPIPostProcessor;

import java.util.Collection;
import java.util.Properties;

/**
 * Required SPI registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequiredSPIRegistry {
    
    /**
     * Get registered service.
     *
     * @param spiClass required SPI class
     * @param <T> SPI class type
     * @return registered service
     */
    public static <T extends RequiredSPI> T getRegisteredService(final Class<T> spiClass) {
        T result = getRequiredService(spiClass);
        if (result instanceof SPIPostProcessor) {
            ((SPIPostProcessor) result).init(new Properties());
        }
        return result;
    }
    
    private static <T extends RequiredSPI> T getRequiredService(final Class<T> spiClass) {
        Collection<T> services = ShardingSphereServiceLoader.getServiceInstances(spiClass);
        if (services.isEmpty()) {
            throw new ServiceProviderNotFoundException(spiClass);
        }
        if (1 == services.size()) {
            return services.iterator().next();
        }
        for (T each : services) {
            if (each.isDefault()) {
                return each;
            }
        }
        return services.iterator().next();
    }
}
