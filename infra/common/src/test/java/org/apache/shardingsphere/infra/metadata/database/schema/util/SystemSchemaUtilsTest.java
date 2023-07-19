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

import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.opengauss.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemSchemaUtilsTest {
    
    @Test
    void assertContainsSystemSchemaForPostgreSQL() {
        ShardingSphereDatabase informationSchemaDatabase = mockShardingSphereDatabase("information_schema", false);
        assertTrue(SystemSchemaUtils.containsSystemSchema(new PostgreSQLDatabaseType(), Arrays.asList("information_schema", "pg_catalog"), informationSchemaDatabase));
        ShardingSphereDatabase shardingSchemaDatabase = mockShardingSphereDatabase("sharding_db", false);
        assertFalse(SystemSchemaUtils.containsSystemSchema(new PostgreSQLDatabaseType(), Collections.singletonList("sharding_db"), shardingSchemaDatabase));
        ShardingSphereDatabase customizedInformationSchemaDatabase = mockShardingSphereDatabase("information_schema", true);
        assertTrue(SystemSchemaUtils.containsSystemSchema(new PostgreSQLDatabaseType(), Arrays.asList("information_schema", "pg_catalog"), customizedInformationSchemaDatabase));
    }
    
    @Test
    void assertContainsSystemSchemaForOpenGaussSQL() {
        ShardingSphereDatabase informationSchemaDatabase = mockShardingSphereDatabase("information_schema", false);
        assertTrue(SystemSchemaUtils.containsSystemSchema(new OpenGaussDatabaseType(), Arrays.asList("information_schema", "pg_catalog"), informationSchemaDatabase));
        ShardingSphereDatabase shardingSchemaDatabase = mockShardingSphereDatabase("sharding_db", false);
        assertFalse(SystemSchemaUtils.containsSystemSchema(new OpenGaussDatabaseType(), Collections.singletonList("sharding_db"), shardingSchemaDatabase));
        ShardingSphereDatabase customizedInformationSchemaDatabase = mockShardingSphereDatabase("information_schema", true);
        assertTrue(SystemSchemaUtils.containsSystemSchema(new OpenGaussDatabaseType(), Arrays.asList("information_schema", "pg_catalog"), customizedInformationSchemaDatabase));
        ShardingSphereDatabase customizedGaussDBDatabase = mockShardingSphereDatabase("gaussdb", true);
        assertFalse(SystemSchemaUtils.containsSystemSchema(new OpenGaussDatabaseType(), Collections.emptyList(), customizedGaussDBDatabase));
    }
    
    @Test
    void assertContainsSystemSchemaForMySQL() {
        ShardingSphereDatabase informationSchemaDatabase = mockShardingSphereDatabase("information_schema", false);
        assertTrue(SystemSchemaUtils.containsSystemSchema(new MySQLDatabaseType(), Arrays.asList("information_schema", "mysql"), informationSchemaDatabase));
        ShardingSphereDatabase shardingSchemaDatabase = mockShardingSphereDatabase("sharding_db", false);
        assertFalse(SystemSchemaUtils.containsSystemSchema(new MySQLDatabaseType(), Collections.singletonList("sharding_db"), shardingSchemaDatabase));
        ShardingSphereDatabase customizedInformationSchemaDatabase = mockShardingSphereDatabase("information_schema", true);
        assertFalse(SystemSchemaUtils.containsSystemSchema(new MySQLDatabaseType(), Arrays.asList("information_schema", "mysql"), customizedInformationSchemaDatabase));
    }
    
    private ShardingSphereDatabase mockShardingSphereDatabase(final String databaseName, final boolean isComplete) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn(databaseName);
        when(result.isComplete()).thenReturn(isComplete);
        return result;
    }
}
