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

package org.apache.shardingsphere.infra.metadata.statistics.collector.postgresql;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.collector.DialectDatabaseStatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class PostgreSQLStatisticsCollectorTest {
    
    private final DialectDatabaseStatisticsCollector collector = DatabaseTypedSPILoader.getService(
            DialectDatabaseStatisticsCollector.class, TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
    
    @Test
    void assertCollectRowColumnValuesWithExistingCollector() throws SQLException {
        assertTrue(collector.collectRowColumnValues("foo_db", "pg_catalog", "pg_class", mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS)).isPresent());
    }
    
    @Test
    void assertCollectRowColumnValuesWithNonExistingCollector() throws SQLException {
        assertFalse(collector.collectRowColumnValues("foo_db", "unknown_schema", "unknown_table", mock()).isPresent());
    }
    
    @Test
    void assertIsStatisticsTablesWithEmptySchemaTables() {
        assertFalse(collector.isStatisticsTables(Collections.emptyMap()));
    }
    
    @Test
    void assertIsStatisticsTablesWithNonExistingSchema() {
        assertFalse(collector.isStatisticsTables(Collections.singletonMap("unknown_schema", Collections.singleton("unknown_table"))));
    }
    
    @Test
    void assertIsStatisticsTablesWithSchemaButMissingTable() {
        assertFalse(collector.isStatisticsTables(Collections.singletonMap("pg_catalog", Collections.singleton("unknown_table"))));
    }
    
    @Test
    void assertIsStatisticsTablesWithValidSchemaAndTables() {
        assertTrue(collector.isStatisticsTables(Collections.singletonMap("pg_catalog", Arrays.asList("pg_class", "pg_namespace"))));
    }
}
