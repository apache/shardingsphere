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

package org.apache.shardingsphere.infra.database.opengauss;

import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenGaussDatabaseTypeTest {
    
    @Test
    void assertGetQuoteCharacter() {
        assertThat(TypedSPILoader.getService(DatabaseType.class, "openGauss").getQuoteCharacter(), is(QuoteCharacter.QUOTE));
    }
    
    @Test
    void assertGetJdbcUrlPrefixes() {
        assertThat(TypedSPILoader.getService(DatabaseType.class, "openGauss").getJdbcUrlPrefixes(), is(Collections.singleton("jdbc:opengauss:")));
    }
    
    @Test
    void assertGetDataSourceMetaData() {
        assertThat(TypedSPILoader.getService(DatabaseType.class, "openGauss").getDataSourceMetaData("jdbc:opengauss://localhost:5432/demo_ds_0", "postgres"),
                instanceOf(OpenGaussDataSourceMetaData.class));
    }
    
    @Test
    void assertGetSystemDatabases() {
        assertTrue(TypedSPILoader.getService(DatabaseType.class, "openGauss").getSystemDatabaseSchemaMap().containsKey("postgres"));
    }
    
    @Test
    void assertGetSystemSchemas() {
        assertThat(TypedSPILoader.getService(DatabaseType.class, "openGauss").getSystemSchemas(), is(new HashSet<>(Arrays.asList("information_schema", "pg_catalog", "blockchain",
                "cstore", "db4ai", "dbe_perf", "dbe_pldebugger", "gaussdb", "oracle", "pkg_service", "snapshot", "sqladvisor", "dbe_pldeveloper", "pg_toast", "pkg_util", "shardingsphere"))));
    }
    
    @Test
    void assertIsSchemaAvailable() {
        assertTrue(TypedSPILoader.getService(DatabaseType.class, "openGauss").isSchemaAvailable());
    }
    
    @Test
    void assertGetDefaultSchema() {
        assertThat(TypedSPILoader.getService(DatabaseType.class, "openGauss").getDefaultSchema(), is(Optional.of("public")));
    }
}
