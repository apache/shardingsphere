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

package org.apache.shardingsphere.database.connector.oracle.metadata.database.system;

import org.apache.shardingsphere.database.connector.core.metadata.database.system.DialectKernelSupportedSystemTable;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

class OracleKernelSupportedSystemTableTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Oracle");
    
    private final DialectKernelSupportedSystemTable kernelSupportedSystemTable = TypedSPILoader.getService(DialectKernelSupportedSystemTable.class, databaseType);
    
    @Test
    void assertGetSchemaAndTablesMap() {
        Map<String, Collection<String>> actual = kernelSupportedSystemTable.getSchemaAndTablesMap();
        assertThat(actual.size(), is(1));
        assertThat(actual.get("SYS"), containsInAnyOrder("ALL_TABLES", "USER_TABLES", "ALL_SEQUENCES", "ALL_VIEWS", "ALL_SYNONYMS", "ALL_TAB_COLUMNS"));
    }
}
