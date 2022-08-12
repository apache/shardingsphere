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

package org.apache.shardingsphere.integration.data.pipeline.framework.comtaner.config;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.framework.comtaner.config.impl.mysql.ScalingMySQLContainerConfiguration;
import org.apache.shardingsphere.integration.data.pipeline.framework.comtaner.config.impl.postgresql.ScalingPostgreSQLContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.impl.mysql.DefaultMySQLContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.impl.opengauss.DefaultOpenGaussContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.impl.postgresql.DefaultPostgreSQLContainerConfiguration;

public final class ScalingStorageContainerConfigurationFactory {
    
    /**
     * Create new instance of storage container.
     *
     * @param databaseType database type
     * @param scenario scenario
     * @return created instance
     */
    public static StorageContainerConfiguration newInstance(final DatabaseType databaseType, final String scenario) {
        switch (databaseType.getType()) {
            case "MySQL":
                if ("default".equalsIgnoreCase(scenario)) {
                    return new DefaultMySQLContainerConfiguration();
                }
                if ("".equalsIgnoreCase(scenario)) {
                    return new ScalingMySQLContainerConfiguration();
                }
                return new DefaultMySQLContainerConfiguration();
            case "PostgreSQL":
                if ("default".equalsIgnoreCase(scenario)) {
                    return new DefaultPostgreSQLContainerConfiguration();
                }
                if ("".equalsIgnoreCase(scenario)) {
                    return new ScalingPostgreSQLContainerConfiguration();
                }
                return new DefaultPostgreSQLContainerConfiguration();
            case "openGauss":
                return new DefaultOpenGaussContainerConfiguration();
            default:
                throw new RuntimeException(String.format("Database `%s` is unknown.", databaseType.getType()));
        }
    }
}
