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

package org.apache.shardingsphere.data.pipeline.util.spi;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.BranchDatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.exception.ServiceProviderNotFoundServerException;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

import java.util.Optional;

/**
 * Pipeline typed SPI loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineTypedSPILoader {
    
    /**
     * Find database typed service.
     *
     * @param spiClass typed SPI class
     * @param databaseType database type
     * @param <T> SPI class type
     * @return service
     */
    public static <T extends TypedSPI> Optional<T> findDatabaseTypedService(final Class<T> spiClass, final String databaseType) {
        Optional<T> result = TypedSPILoader.findService(spiClass, databaseType);
        if (result.isPresent()) {
            return result;
        }
        Optional<DatabaseType> type = TypedSPILoader.findService(DatabaseType.class, databaseType);
        if (type.isPresent() && type.get() instanceof BranchDatabaseType) {
            return TypedSPILoader.findService(spiClass, ((BranchDatabaseType) type.get()).getTrunkDatabaseType().getType());
        }
        return result;
    }
    
    /**
     * Get database typed service.
     *
     * @param spiClass typed SPI class
     * @param databaseType database type
     * @param <T> SPI class type
     * @return service
     */
    public static <T extends TypedSPI> T getDatabaseTypedService(final Class<T> spiClass, final String databaseType) {
        return findDatabaseTypedService(spiClass, databaseType).orElseThrow(() -> new ServiceProviderNotFoundServerException(spiClass));
    }
}
