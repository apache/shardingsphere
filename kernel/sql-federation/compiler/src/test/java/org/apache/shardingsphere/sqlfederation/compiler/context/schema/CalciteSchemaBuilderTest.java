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

package org.apache.shardingsphere.sqlfederation.compiler.context.schema;

import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqlfederation.compiler.sql.function.DialectSQLFederationFunctionRegister;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

class CalciteSchemaBuilderTest {
    
    @Test
    void assertBuildWithoutSchemas() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ShardingSphereDatabase databaseWithoutSchemas = new ShardingSphereDatabase(
                "empty_db", databaseType, mock(ResourceMetaData.class), new RuleMetaData(Collections.emptyList()), Collections.emptyList());
        CalciteSchemaBuilder.build(Collections.emptyList());
        CalciteSchema actual = CalciteSchemaBuilder.build(Collections.singleton(databaseWithoutSchemas));
        assertFalse(actual.getSubSchemaMap().containsKey("empty_db"));
    }
    
    @Test
    void assertBuildWithDefaultSchemaRegistersNestedFunctions() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        Collection<ShardingSphereSchema> schemas = Arrays.asList(new ShardingSphereSchema("public", mock(DatabaseType.class)),
                new ShardingSphereSchema("other", mock(DatabaseType.class)));
        ShardingSphereDatabase database = new ShardingSphereDatabase("pg_db", databaseType, mock(ResourceMetaData.class), new RuleMetaData(Collections.emptyList()), schemas);
        DialectSQLFederationFunctionRegister functionRegister = mock(DialectSQLFederationFunctionRegister.class);
        try (MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class, CALLS_REAL_METHODS)) {
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectSQLFederationFunctionRegister.class, databaseType))
                    .thenReturn(Optional.of(functionRegister)).thenReturn(Optional.empty());
            CalciteSchema actual = CalciteSchemaBuilder.build(Collections.singletonList(database));
            assertTrue(actual.getSubSchemaMap().containsKey("pg_db"));
            CalciteSchema databaseSchema = actual.getSubSchema("pg_db", true);
            assertNotNull(databaseSchema);
            assertTrue(databaseSchema.getSubSchemaMap().keySet().containsAll(Arrays.asList("public", "other")));
            ArgumentCaptor<String> schemaNameCaptor = ArgumentCaptor.forClass(String.class);
            verify(functionRegister).registerFunction(any(SchemaPlus.class), schemaNameCaptor.capture());
            assertThat(Arrays.asList("public", "other"), hasItem(schemaNameCaptor.getValue()));
        }
    }
    
    @Test
    void assertBuildWithoutDefaultSchemaRegistersFunctions() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ShardingSphereDatabase database = new ShardingSphereDatabase("mysql_db", databaseType, mock(ResourceMetaData.class),
                new RuleMetaData(Collections.emptyList()), Collections.singletonList(new ShardingSphereSchema("foo_schema", mock(DatabaseType.class))));
        try (MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class, CALLS_REAL_METHODS)) {
            DialectSQLFederationFunctionRegister functionRegister = mock(DialectSQLFederationFunctionRegister.class);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectSQLFederationFunctionRegister.class, databaseType))
                    .thenReturn(Optional.empty()).thenReturn(Optional.of(functionRegister));
            CalciteSchema actual = CalciteSchemaBuilder.build(Collections.singletonList(database));
            assertTrue(actual.getSubSchemaMap().containsKey("mysql_db"));
            verify(functionRegister).registerFunction(any(SchemaPlus.class), eq("mysql_db"));
        }
    }
}
