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

package org.apache.shardingsphere.database.connector.opengauss.metadata.database.system;

import org.apache.shardingsphere.database.connector.core.metadata.database.system.DialectSystemDatabase;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class OpenGaussSystemDatabaseTest {
    
    private static final Collection<String> EXPECTED_SYSTEM_SCHEMAS = Arrays.asList("information_schema", "pg_catalog", "blockchain",
            "cstore", "db4ai", "dbe_perf", "dbe_pldebugger", "gaussdb", "oracle", "pkg_service", "snapshot", "sqladvisor", "dbe_pldeveloper", "pg_toast", "pkg_util", "shardingsphere");
    
    private final DialectSystemDatabase systemDatabase = DatabaseTypedSPILoader.getService(DialectSystemDatabase.class, TypedSPILoader.getService(DatabaseType.class, "openGauss"));
    
    @Test
    void assertGetSystemDatabases() {
        assertThat(systemDatabase.getSystemDatabases(), is(Collections.singleton("postgres")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSystemSchemasWithDatabaseNameArguments")
    void assertGetSystemSchemasWithDatabaseName(final String name, final String databaseName, final Collection<String> expectedSchemas) {
        assertThat(systemDatabase.getSystemSchemas(databaseName), is(expectedSchemas));
    }
    
    private static Stream<Arguments> getSystemSchemasWithDatabaseNameArguments() {
        return Stream.of(
                Arguments.of("existing database", "postgres", EXPECTED_SYSTEM_SCHEMAS),
                Arguments.of("non-existing database", "foo", Collections.emptyList()),
                Arguments.of("null database", null, Collections.emptyList()));
    }
    
    @Test
    void assertGetSystemSchemas() {
        assertThat(systemDatabase.getSystemSchemas(), is(EXPECTED_SYSTEM_SCHEMAS));
    }
}
