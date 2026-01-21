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

package org.apache.shardingsphere.data.pipeline.cdc.util;

import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody.SchemaTable;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CDCSchemaTableUtilsTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    @Test
    void assertParseTableExpressionWithFullWildcard() {
        ShardingSphereSchema publicSchema = mockSchema("public", "t_order", "t_order_item");
        ShardingSphereSchema testSchema = mockSchema("test", "t_test");
        ShardingSphereSchema systemSchema = mockSchema("pg_catalog", "t_pg");
        ShardingSphereDatabase database = new ShardingSphereDatabase("sharding_db", databaseType, null, null, Arrays.asList(publicSchema, testSchema, systemSchema));
        List<SchemaTable> schemaTables = Collections.singletonList(SchemaTable.newBuilder().setSchema("*").setTable("*").build());
        Map<String, Set<String>> actualResult = CDCSchemaTableUtils.parseTableExpressionWithSchema(database, schemaTables);
        Map<String, Set<String>> expectedResult = new HashMap<>(2, 1F);
        expectedResult.put("public", new HashSet<>(Arrays.asList("t_order", "t_order_item")));
        expectedResult.put("test", Collections.singleton("t_test"));
        assertThat(actualResult, is(expectedResult));
    }
    
    @Test
    void assertParseTableExpressionWithSchemaWildcard() {
        ShardingSphereSchema analyticsSchema = mockSchema("analytics", "t_shared");
        ShardingSphereSchema auditSchema = mockSchema("audit", "t_other");
        ShardingSphereSchema systemSchema = mockSchema("pg_catalog", "t_shared");
        ShardingSphereDatabase database = new ShardingSphereDatabase("sharding_db", databaseType, null, null, Arrays.asList(analyticsSchema, auditSchema, systemSchema));
        List<SchemaTable> schemaTables = Collections.singletonList(SchemaTable.newBuilder().setSchema("*").setTable("t_shared").build());
        Map<String, Set<String>> actualResult = CDCSchemaTableUtils.parseTableExpressionWithSchema(database, schemaTables);
        Map<String, Set<String>> expectedResult = Collections.singletonMap("analytics", Collections.singleton("t_shared"));
        assertThat(actualResult, is(expectedResult));
    }
    
    @Test
    void assertParseTableExpressionWithAllTablesInSchema() {
        ShardingSphereSchema publicSchema = mockSchema("public", "t_order", "t_order_item");
        ShardingSphereDatabase database = new ShardingSphereDatabase("sharding_db", databaseType, null, null, Collections.singleton(publicSchema));
        List<SchemaTable> schemaTables = Collections.singletonList(SchemaTable.newBuilder().setSchema("public").setTable("*").build());
        Map<String, Set<String>> actualResult = CDCSchemaTableUtils.parseTableExpressionWithSchema(database, schemaTables);
        Map<String, Set<String>> expectedResult = Collections.singletonMap("public", new HashSet<>(Arrays.asList("t_order", "t_order_item")));
        assertThat(actualResult, is(expectedResult));
    }
    
    @Test
    void assertParseTableExpressionFillDefaultSchema() {
        ShardingSphereSchema publicSchema = mockSchema("public", "t_order");
        ShardingSphereDatabase database = new ShardingSphereDatabase("sharding_db", databaseType, null, null, Collections.singleton(publicSchema));
        List<SchemaTable> schemaTables = Collections.singletonList(SchemaTable.newBuilder().setTable("t_order").build());
        Map<String, Set<String>> actualResult = CDCSchemaTableUtils.parseTableExpressionWithSchema(database, schemaTables);
        Map<String, Set<String>> expectedResult = Collections.singletonMap("public", Collections.singleton("t_order"));
        assertThat(actualResult, is(expectedResult));
    }
    
    @Test
    void assertParseTableExpressionWithMissingTable() {
        ShardingSphereSchema publicSchema = mockSchema("public", "t_exist");
        ShardingSphereDatabase database = new ShardingSphereDatabase("sharding_db", databaseType, null, null, Collections.singleton(publicSchema));
        List<SchemaTable> schemaTables = Collections.singletonList(SchemaTable.newBuilder().setSchema("public").setTable("t_missing").build());
        assertThrows(TableNotFoundException.class, () -> CDCSchemaTableUtils.parseTableExpressionWithSchema(database, schemaTables));
    }
    
    @Test
    void assertParseTableExpressionWithoutSchema() {
        ShardingSphereSchema publicSchema = mockSchema("public", "t_order", "t_order2");
        ShardingSphereDatabase database = new ShardingSphereDatabase("public", TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), null, null, Collections.singleton(publicSchema));
        List<String> tableNames = Collections.singletonList("*");
        Collection<String> actualWildcardTable = CDCSchemaTableUtils.parseTableExpressionWithoutSchema(database, tableNames);
        Set<String> expectedWildcardTable = new HashSet<>(Arrays.asList("t_order", "t_order2"));
        assertThat(actualWildcardTable, is(expectedWildcardTable));
        ShardingSphereDatabase databaseWithoutSchema = new ShardingSphereDatabase("missing", TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), null, null, Collections.emptyList());
        List<String> singleTable = Collections.singletonList("t_order");
        Collection<String> actualTableNames = CDCSchemaTableUtils.parseTableExpressionWithoutSchema(databaseWithoutSchema, singleTable);
        Set<String> expectedTableNames = new HashSet<>(singleTable);
        assertThat(actualTableNames, is(expectedTableNames));
    }
    
    private ShardingSphereSchema mockSchema(final String schemaName, final String... tableNames) {
        Collection<ShardingSphereTable> tables = new ArrayList<>(tableNames.length);
        for (String each : tableNames) {
            tables.add(mockTable(each));
        }
        return new ShardingSphereSchema(schemaName, tables, Collections.emptyList(), databaseType);
    }
    
    private ShardingSphereTable mockTable(final String tableName) {
        ShardingSphereTable result = mock(ShardingSphereTable.class);
        when(result.getName()).thenReturn(tableName);
        return result;
    }
}
