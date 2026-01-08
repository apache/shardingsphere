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

package org.apache.shardingsphere.database.connector.core.spi;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Optional;
import java.util.Properties;

/**
 * Database typed SPI loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseTypedSPILoader {
    
    /**
     * Find service.
     *
     * @param spiClass typed SPI class
     * @param databaseType database type
     * @param <T> SPI class type
     * @return found service
     */
    public static <T extends DatabaseTypedSPI> Optional<T> findService(final Class<T> spiClass, final DatabaseType databaseType) {
        Optional<T> result = TypedSPILoader.findService(spiClass, databaseType);
        if (result.isPresent()) {
            return result;
        }
        Optional<DatabaseType> trunkDatabaseType = databaseType.getTrunkDatabaseType();
        return trunkDatabaseType.isPresent() ? TypedSPILoader.findService(spiClass, trunkDatabaseType.get()) : result;
    }
    
    /**
     * Find service.
     *
     * @param spiClass typed SPI class
     * @param databaseType database type
     * @param props properties
     * @param <T> SPI class type
     * @return found service
     */
    public static <T extends DatabaseTypedSPI> Optional<T> findService(final Class<T> spiClass, final DatabaseType databaseType, final Properties props) {
        Optional<T> result = TypedSPILoader.findService(spiClass, databaseType, props);
        if (result.isPresent()) {
            return result;
        }
        Optional<DatabaseType> trunkDatabaseType = databaseType.getTrunkDatabaseType();
        return trunkDatabaseType.isPresent() ? TypedSPILoader.findService(spiClass, trunkDatabaseType.get(), props) : result;
    }
    
    /**
     * Get service.
     *
     * @param spiClass typed SPI class
     * @param databaseType database type
     * @param <T> SPI class type
     * @return found service
     */
    public static <T extends DatabaseTypedSPI> T getService(final Class<T> spiClass, final DatabaseType databaseType) {
        return findService(spiClass, databaseType).orElseGet(() -> TypedSPILoader.getService(spiClass, null));
    }
    
    /**
     * Get service.
     *
     * @param spiClass typed SPI class
     * @param databaseType database type
     * @param props properties
     * @param <T> SPI class type
     * @return found service
     */
    public static <T extends DatabaseTypedSPI> T getService(final Class<T> spiClass, final DatabaseType databaseType, final Properties props) {
        return findService(spiClass, databaseType, props).orElseThrow(() -> new ServiceProviderNotFoundException(spiClass, databaseType.getType()));
    }
}
