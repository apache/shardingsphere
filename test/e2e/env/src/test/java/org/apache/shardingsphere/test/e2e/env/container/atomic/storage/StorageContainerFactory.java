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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.option.StorageContainerConfigurationOption;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.type.docker.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;

/**
 * Storage container factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageContainerFactory {
    
    /**
     * Create new instance of storage container.
     *
     * @param databaseType database type
     * @param option storage container configuration option
     * @param scenario scenario
     * @return created instance
     * @throws RuntimeException runtime exception
     */
    public static StorageContainer newInstance(final DatabaseType databaseType, final StorageContainerConfigurationOption option, final String scenario) {
        return newInstance(databaseType, E2ETestEnvironment.getInstance().getClusterEnvironment().getDatabaseImages().get(databaseType), option, scenario);
    }
    
    /**
     * Create new instance of storage container.
     *
     * @param databaseType database type
     * @param storageContainerImage storage container image
     * @param option storage container configuration option
     * @param scenario scenario
     * @return created instance
     * @throws RuntimeException runtime exception
     */
    public static StorageContainer newInstance(final DatabaseType databaseType, final String storageContainerImage, final StorageContainerConfigurationOption option, final String scenario) {
        switch (databaseType.getType()) {
            case "MySQL":
            case "PostgreSQL":
            case "openGauss":
            case "MariaDB":
                return new DockerStorageContainer(storageContainerImage, option, scenario);
            case "Hive":
                return new DockerStorageContainer(storageContainerImage, option, scenario);
            default:
                throw new RuntimeException(String.format("Database `%s` is unknown.", databaseType.getType()));
        }
    }
}
