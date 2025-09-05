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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.option;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.option.dialect.H2StorageContainerConfigurationOption;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.option.dialect.MariaDBStorageContainerConfigurationOption;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.option.dialect.MySQLStorageContainerConfigurationOption;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.option.dialect.OpenGaussStorageContainerConfigurationOption;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.option.dialect.PostgreSQLStorageContainerConfigurationOption;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.option.dialect.HiveStorageContainerConfigurationOption;

/**
 * Storage container configuration option factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageContainerConfigurationOptionFactory {
    
    /**
     * Create new instance of storage container configuration option.
     *
     * @param databaseType database type
     * @return created storage container configuration option
     * @throws RuntimeException runtime exception
     */
    public static StorageContainerConfigurationOption newInstance(final DatabaseType databaseType) {
        switch (databaseType.getType()) {
            case "MySQL":
                return new MySQLStorageContainerConfigurationOption();
            case "MariaDB":
                return new MariaDBStorageContainerConfigurationOption();
            case "PostgreSQL":
                return new PostgreSQLStorageContainerConfigurationOption();
            case "openGauss":
                return new OpenGaussStorageContainerConfigurationOption();
            case "H2":
                return new H2StorageContainerConfigurationOption();
            case "Hive":
                return new HiveStorageContainerConfigurationOption();
            default:
                throw new RuntimeException(String.format("Database `%s` is unknown.", databaseType.getType()));
        }
    }
}
