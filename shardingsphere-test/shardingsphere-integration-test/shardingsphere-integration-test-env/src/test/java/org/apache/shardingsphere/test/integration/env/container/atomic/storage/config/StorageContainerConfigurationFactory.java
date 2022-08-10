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

package org.apache.shardingsphere.test.integration.env.container.atomic.storage.config;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.impl.mysql.DefaultMySQLContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.impl.mysql.ScalingMySQLContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.impl.opengauss.DefaultOpenGaussContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.impl.postgresql.DefaultPostgreSQLContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.impl.postgresql.ScalingPostgreSQLContainerConfiguration;

public class StorageContainerConfigurationFactory {
    
    /**
     * Create new instance of storage container.
     *
     * @param databaseType database type
     * @param module module
     * @param scenario scenario
     * @return created instance
     */
    public static StorageContainerConfiguration newInstance(final DatabaseType databaseType, final String scenario, final String module) {
        switch (databaseType.getType()) {
            case "MySQL":
                if ("scaling".equalsIgnoreCase(module)) {
                    return new ScalingMySQLContainerConfiguration(scenario);
                }
                return new DefaultMySQLContainerConfiguration(scenario);
            case "PostgreSQL":
                if ("scaling".equalsIgnoreCase(module)) {
                    return new ScalingPostgreSQLContainerConfiguration(scenario);
                }
                return new DefaultPostgreSQLContainerConfiguration(scenario);
            case "openGauss":
                return new DefaultOpenGaussContainerConfiguration(scenario);
            default:
                throw new RuntimeException(String.format("Database `%s` is unknown.", databaseType.getType()));
        }
    }
}
