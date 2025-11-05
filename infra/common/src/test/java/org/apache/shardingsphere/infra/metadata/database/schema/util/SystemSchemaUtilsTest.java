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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
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
        ShardingSphereDatabase informationSchemaDatabase = mockDatabase("information_schema", false);
        assertTrue(SystemSchemaUtils.containsSystemSchema(databaseType, Arrays.asList("information_schema", "mysql"), informationSchemaDatabase));
        ShardingSphereDatabase shardingSchemaDatabase = mockDatabase("sharding_db", false);
        assertFalse(SystemSchemaUtils.containsSystemSchema(databaseType, Collections.singletonList("sharding_db"), shardingSchemaDatabase));
        ShardingSphereDatabase customizedInformationSchemaDatabase = mockDatabase("information_schema", true);
        assertFalse(SystemSchemaUtils.containsSystemSchema(databaseType, Arrays.asList("information_schema", "mysql"), customizedInformationSchemaDatabase));
    }
    
    @Test
    void assertContainsSystemSchemaForPostgreSQL() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        ShardingSphereDatabase informationSchemaDatabase = mockDatabase("information_schema", false);
        assertTrue(SystemSchemaUtils.containsSystemSchema(databaseType, Arrays.asList("information_schema", "pg_catalog"), informationSchemaDatabase));
        ShardingSphereDatabase shardingSchemaDatabase = mockDatabase("sharding_db", false);
        assertFalse(SystemSchemaUtils.containsSystemSchema(databaseType, Collections.singletonList("sharding_db"), shardingSchemaDatabase));
        ShardingSphereDatabase customizedInformationSchemaDatabase = mockDatabase("information_schema", true);
        assertTrue(SystemSchemaUtils.containsSystemSchema(databaseType, Arrays.asList("information_schema", "pg_catalog"), customizedInformationSchemaDatabase));
    }
    
    @Test
    void assertContainsSystemSchemaForOpenGaussSQL() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
        ShardingSphereDatabase informationSchemaDatabase = mockDatabase("information_schema", false);
        assertTrue(SystemSchemaUtils.containsSystemSchema(databaseType, Arrays.asList("information_schema", "pg_catalog"), informationSchemaDatabase));
        ShardingSphereDatabase shardingSchemaDatabase = mockDatabase("sharding_db", false);
        assertFalse(SystemSchemaUtils.containsSystemSchema(databaseType, Collections.singletonList("sharding_db"), shardingSchemaDatabase));
        ShardingSphereDatabase customizedInformationSchemaDatabase = mockDatabase("information_schema", true);
        assertTrue(SystemSchemaUtils.containsSystemSchema(databaseType, Arrays.asList("information_schema", "pg_catalog"), customizedInformationSchemaDatabase));
        ShardingSphereDatabase customizedGaussDBDatabase = mockDatabase("gaussdb", true);
        assertFalse(SystemSchemaUtils.containsSystemSchema(databaseType, Collections.emptyList(), customizedGaussDBDatabase));
    }
    
    @Test
    void assertIsSystemSchemaWithUnCompleteDatabase() {
        ShardingSphereDatabase informationSchemaDatabase = mockDatabase("information_schema", false);
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        when(informationSchemaDatabase.getProtocolType()).thenReturn(databaseType);
        assertTrue(SystemSchemaUtils.isSystemSchema(informationSchemaDatabase));
    }
    
    @Test
    void assertIsSystemSchemaWithCompleteDatabaseAndDefaultSchema() {
        ShardingSphereDatabase pgCatalogDatabase = mockDatabase("pg_catalog", true);
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        when(pgCatalogDatabase.getProtocolType()).thenReturn(databaseType);
        assertTrue(SystemSchemaUtils.isSystemSchema(pgCatalogDatabase));
    }
    
    @Test
    void assertIsSystemSchemaWithEmptyDatabase() {
        ShardingSphereDatabase userDatabase = mockDatabase("foo_db", true);
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
        when(userDatabase.getProtocolType()).thenReturn(databaseType);
        assertFalse(SystemSchemaUtils.isSystemSchema(userDatabase));
    }
    
    @Test
    void assertIsSystemSchemaWithoutDefaultSchema() {
        ShardingSphereDatabase userDatabase = mockDatabase("foo_db", false);
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
        when(userDatabase.getProtocolType()).thenReturn(databaseType);
        assertFalse(SystemSchemaUtils.isSystemSchema(userDatabase));
    }
    
    @Test
    void assertIsDriverQuerySystemCatalogWithoutOption() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ExpressionProjectionSegment projection = new ExpressionProjectionSegment(0, 10, "version()");
        assertFalse(SystemSchemaUtils.isDriverQuerySystemCatalog(databaseType, Collections.singleton(projection)));
    }
    
    @Test
    void assertIsDriverQuerySystemCatalogWithMultipleProjections() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
        ExpressionProjectionSegment projection1 = new ExpressionProjectionSegment(0, 10, "version()");
        ExpressionProjectionSegment projection2 = new ExpressionProjectionSegment(11, 20, "current_database()");
        assertFalse(SystemSchemaUtils.isDriverQuerySystemCatalog(databaseType, Arrays.asList(projection1, projection2)));
    }
    
    @Test
    void assertIsDriverQuerySystemCatalogWithNonExpressionProjection() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
        assertFalse(SystemSchemaUtils.isDriverQuerySystemCatalog(databaseType, Collections.singleton(mock(ProjectionSegment.class))));
    }
    
    @Test
    void assertIsDriverQuerySystemCatalogWithValidExpression() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
        ExpressionProjectionSegment projection = new ExpressionProjectionSegment(0, 10, "version()");
        assertTrue(SystemSchemaUtils.isDriverQuerySystemCatalog(databaseType, Collections.singleton(projection)));
    }
    
    private ShardingSphereDatabase mockDatabase(final String databaseName, final boolean isComplete) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn(databaseName);
        when(result.isComplete()).thenReturn(isComplete);
        return result;
    }
}
