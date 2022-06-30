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

package org.apache.shardingsphere.integration.data.pipeline.factory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.database.DatabaseContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.database.MySQLContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.database.OpenGaussContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.database.PostgreSQLContainer;

/**
 * Storage container factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseContainerFactory {
    
    /**
     * Create new instance of storage container.
     *
     * @param databaseType database type
     * @param dockerImageName database ver
     * @return created instance
     */
    public static DatabaseContainer newInstance(final DatabaseType databaseType, final String dockerImageName) {
        switch (databaseType.getType()) {
            case "MySQL":
                return new MySQLContainer(dockerImageName);
            case "PostgreSQL":
                return new PostgreSQLContainer(dockerImageName);
            case "openGauss":
                return new OpenGaussContainer(dockerImageName);
            default:
                throw new RuntimeException(String.format("Database [%s] is unknown.", databaseType.getType()));
        }
    }
}
