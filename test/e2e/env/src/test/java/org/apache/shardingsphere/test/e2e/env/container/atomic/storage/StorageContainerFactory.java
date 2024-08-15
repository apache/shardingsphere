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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.H2Container;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.MariaDBContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.MySQLContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.OpenGaussContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.PostgreSQLContainer;

import java.util.Collection;
import java.util.Collections;

/**
 * Storage container factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageContainerFactory {
    
    /**
     * Create new instance of storage container.
     *
     * @param databaseType database type
     * @param storageContainerImage storage container image
     * @param storageContainerConfig storage container configuration
     * @return created instance
     * @throws RuntimeException runtime exception
     */
    public static StorageContainer newInstance(final DatabaseType databaseType, final String storageContainerImage, final StorageContainerConfiguration storageContainerConfig) {
        return newInstance(databaseType, storageContainerImage, storageContainerConfig, Collections.emptyList());
    }
    
    /**
     * Create new instance of storage container.
     *
     * @param databaseType database type
     * @param storageContainerImage storage container image
     * @param storageContainerConfig storage container configuration
     * @param databases databases
     * @return created instance
     * @throws RuntimeException runtime exception
     */
    public static StorageContainer newInstance(final DatabaseType databaseType, final String storageContainerImage,
                                               final StorageContainerConfiguration storageContainerConfig, final Collection<String> databases) {
        switch (databaseType.getType()) {
            case "MySQL":
                return new MySQLContainer(storageContainerImage, storageContainerConfig, databases);
            case "PostgreSQL":
                return new PostgreSQLContainer(storageContainerImage, storageContainerConfig, databases);
            case "openGauss":
                return new OpenGaussContainer(storageContainerImage, storageContainerConfig, databases);
            case "H2":
                return new H2Container(storageContainerConfig, databases);
            case "MariaDB":
                return new MariaDBContainer(storageContainerImage, storageContainerConfig, databases);
            default:
                throw new RuntimeException(String.format("Database `%s` is unknown.", databaseType.getType()));
        }
    }
}
