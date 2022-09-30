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

package org.apache.shardingsphere.test.integration.ha.framework.container.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.integration.ha.framework.container.config.mysql.MySQLContainerConfigurationFactory;

import java.util.List;

/**
 * Storage container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageContainerConfigurationFactory {
    
    /**
     * Create new instance of storage container configuration.
     * 
     * @param scenario scenario
     * @param databaseType database type
     * @return created instance
     */
    public static List<StorageContainerConfiguration> newInstance(final String scenario, final DatabaseType databaseType) {
        switch (databaseType.getType()) {
            case "MySQL":
                return MySQLContainerConfigurationFactory.newInstance(scenario, databaseType);
            // TODO please add other configuration factory for PG or OG if there is HA solution for these database types.
            default:
                throw new RuntimeException(String.format("Unknown Database `%s`", databaseType.getType()));
        }
    }
}
