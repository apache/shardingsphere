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
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.H2Container;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.MySQLContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.OpenGaussContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.PostgreSQLContainer;

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
     * @param scenario scenario
     * @param storageContainerConfiguration storage container configuration
     * @return created instance
     * @throws RuntimeException runtime exception
     */
    public static StorageContainer newInstance(final DatabaseType databaseType, final String storageContainerImage, final String scenario,
                                               final StorageContainerConfiguration storageContainerConfiguration) {
        switch (databaseType.getType()) {
            case "MySQL":
                return new MySQLContainer(storageContainerImage, scenario, storageContainerConfiguration);
            case "PostgreSQL":
                return new PostgreSQLContainer(storageContainerImage, scenario, storageContainerConfiguration);
            case "openGauss":
                return new OpenGaussContainer(storageContainerImage, scenario, storageContainerConfiguration);
            case "H2":
                return new H2Container(scenario);
            default:
                throw new RuntimeException(String.format("Database `%s` is unknown.", databaseType.getType()));
        }
    }
}
