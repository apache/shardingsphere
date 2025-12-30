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

package org.apache.shardingsphere.database.connector.postgresql.metadata.database.system;

import org.apache.shardingsphere.database.connector.core.metadata.database.system.DialectKernelSupportedSystemTable;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PostgreSQLKernelSupportedSystemTableTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final DialectKernelSupportedSystemTable kernelSupportedSystemTable = TypedSPILoader.getService(DialectKernelSupportedSystemTable.class, databaseType);
    
    @Test
    void assertGetSchemaAndTablesMap() {
        Map<String, Collection<String>> actual = kernelSupportedSystemTable.getSchemaAndTablesMap();
        assertThat(actual.size(), is(2));
        assertThat(actual.get("information_schema"), hasItems("columns", "tables", "views"));
        assertThat(actual.get("pg_catalog"), hasItems("pg_aggregate", "pg_class", "pg_database", "pg_tables", "pg_inherits", "pg_tablespace", "pg_trigger", "pg_namespace", "pg_roles"));
    }
}
