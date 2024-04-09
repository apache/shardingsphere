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

package org.apache.shardingsphere.infra.metadata.database.schema.util;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemSchemaUtilsTest {
    
    @Test
    void assertContainsSystemSchemaForMySQL() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ShardingSphereDatabase informationSchemaDatabase = mockShardingSphereDatabase("information_schema", false);
        assertTrue(SystemSchemaUtils.containsSystemSchema(databaseType, Arrays.asList("information_schema", "mysql"), informationSchemaDatabase));
        ShardingSphereDatabase shardingSchemaDatabase = mockShardingSphereDatabase("sharding_db", false);
        assertFalse(SystemSchemaUtils.containsSystemSchema(databaseType, Collections.singletonList("sharding_db"), shardingSchemaDatabase));
        ShardingSphereDatabase customizedInformationSchemaDatabase = mockShardingSphereDatabase("information_schema", true);
        assertFalse(SystemSchemaUtils.containsSystemSchema(databaseType, Arrays.asList("information_schema", "mysql"), customizedInformationSchemaDatabase));
    }
    
    @Test
    void assertContainsSystemSchemaForPostgreSQL() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        ShardingSphereDatabase informationSchemaDatabase = mockShardingSphereDatabase("information_schema", false);
        assertTrue(SystemSchemaUtils.containsSystemSchema(databaseType, Arrays.asList("information_schema", "pg_catalog"), informationSchemaDatabase));
        ShardingSphereDatabase shardingSchemaDatabase = mockShardingSphereDatabase("sharding_db", false);
        assertFalse(SystemSchemaUtils.containsSystemSchema(databaseType, Collections.singletonList("sharding_db"), shardingSchemaDatabase));
        ShardingSphereDatabase customizedInformationSchemaDatabase = mockShardingSphereDatabase("information_schema", true);
        assertTrue(SystemSchemaUtils.containsSystemSchema(databaseType, Arrays.asList("information_schema", "pg_catalog"), customizedInformationSchemaDatabase));
    }
    
    @Test
    void assertContainsSystemSchemaForOpenGaussSQL() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
        ShardingSphereDatabase informationSchemaDatabase = mockShardingSphereDatabase("information_schema", false);
        assertTrue(SystemSchemaUtils.containsSystemSchema(databaseType, Arrays.asList("information_schema", "pg_catalog"), informationSchemaDatabase));
        ShardingSphereDatabase shardingSchemaDatabase = mockShardingSphereDatabase("sharding_db", false);
        assertFalse(SystemSchemaUtils.containsSystemSchema(databaseType, Collections.singletonList("sharding_db"), shardingSchemaDatabase));
        ShardingSphereDatabase customizedInformationSchemaDatabase = mockShardingSphereDatabase("information_schema", true);
        assertTrue(SystemSchemaUtils.containsSystemSchema(databaseType, Arrays.asList("information_schema", "pg_catalog"), customizedInformationSchemaDatabase));
        ShardingSphereDatabase customizedGaussDBDatabase = mockShardingSphereDatabase("gaussdb", true);
        assertFalse(SystemSchemaUtils.containsSystemSchema(databaseType, Collections.emptyList(), customizedGaussDBDatabase));
    }
    
    private ShardingSphereDatabase mockShardingSphereDatabase(final String databaseName, final boolean isComplete) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn(databaseName);
        when(result.isComplete()).thenReturn(isComplete);
        return result;
    }
}
