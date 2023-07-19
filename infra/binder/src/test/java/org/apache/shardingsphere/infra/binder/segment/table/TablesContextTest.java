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

package org.apache.shardingsphere.infra.binder.segment.table;

import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TablesContextTest {
    
    @Test
    void assertGetTableNames() {
        TablesContext tablesContext = new TablesContext(Arrays.asList(createTableSegment("table_1", "tbl_1"),
                createTableSegment("table_2", "tbl_2")), DatabaseTypeFactory.get("MySQL"));
        assertThat(tablesContext.getTableNames(), is(new HashSet<>(Arrays.asList("table_1", "table_2"))));
    }
    
    @Test
    void assertInstanceCreatedWhenNoExceptionThrown() {
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(0, 10, new IdentifierValue("tbl")));
        tableSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("schema")));
        TablesContext tablesContext = new TablesContext(Collections.singleton(tableSegment), DatabaseTypeFactory.get("MySQL"));
        assertThat(tablesContext.getDatabaseName(), is(Optional.of("schema")));
        assertThat(tablesContext.getSchemaName(), is(Optional.of("schema")));
        assertThat(tablesContext.getTableNames(), is(Collections.singleton("tbl")));
        assertTrue(tablesContext.getSubqueryTables().isEmpty());
    }
    
    @Test
    void assertFindTableNameWhenSingleTable() {
        SimpleTableSegment tableSegment = createTableSegment("table_1", "tbl_1");
        ColumnSegment columnSegment = createColumnSegment(null, "col");
        Map<String, String> actual = new TablesContext(Collections.singletonList(tableSegment), DatabaseTypeFactory.get("MySQL"))
                .findTableNamesByColumnSegment(Collections.singletonList(columnSegment), mock(ShardingSphereSchema.class));
        assertFalse(actual.isEmpty());
        assertThat(actual.get("col"), is("table_1"));
    }
    
    @Test
    void assertFindTableNameWhenColumnSegmentOwnerPresent() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        ColumnSegment columnSegment = createColumnSegment("table_1", "col");
        Map<String, String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeFactory.get("MySQL"))
                .findTableNamesByColumnSegment(Collections.singletonList(columnSegment), mock(ShardingSphereSchema.class));
        assertFalse(actual.isEmpty());
        assertThat(actual.get("table_1.col"), is("table_1"));
    }
    
    @Test
    void assertFindTableNameWhenColumnSegmentOwnerAbsent() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        ColumnSegment columnSegment = createColumnSegment(null, "col");
        Map<String, String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeFactory.get("MySQL"))
                .findTableNamesByColumnSegment(Collections.singletonList(columnSegment), mock(ShardingSphereSchema.class));
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertFindTableNameWhenColumnSegmentOwnerAbsentAndSchemaMetaDataContainsColumn() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getAllColumnNames("table_1")).thenReturn(Collections.singletonList("col"));
        ColumnSegment columnSegment = createColumnSegment(null, "col");
        Map<String, String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2),
                DatabaseTypeFactory.get("MySQL")).findTableNamesByColumnSegment(Collections.singletonList(columnSegment), schema);
        assertFalse(actual.isEmpty());
        assertThat(actual.get("col"), is("table_1"));
    }
    
    @Test
    void assertFindTableNameWhenColumnSegmentOwnerAbsentAndSchemaMetaDataContainsColumnInUpperCase() {
        SimpleTableSegment tableSegment1 = createTableSegment("TABLE_1", "TBL_1");
        SimpleTableSegment tableSegment2 = createTableSegment("TABLE_2", "TBL_2");
        ShardingSphereTable table = new ShardingSphereTable("TABLE_1",
                Collections.singletonList(new ShardingSphereColumn("COL", 0, false, false, true, true, false)), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema(Stream.of(table).collect(Collectors.toMap(ShardingSphereTable::getName, value -> value)), Collections.emptyMap());
        ColumnSegment columnSegment = createColumnSegment(null, "COL");
        Map<String, String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2),
                DatabaseTypeFactory.get("MySQL")).findTableNamesByColumnSegment(Collections.singletonList(columnSegment), schema);
        assertFalse(actual.isEmpty());
        assertThat(actual.get("col"), is("TABLE_1"));
    }
    
    @Test
    void assertFindTableNameWhenColumnProjectionWhenSingleTable() {
        SimpleTableSegment tableSegment = createTableSegment("table_1", "tbl_1");
        ColumnProjection columnProjection = new ColumnProjection(null, "col", "cl");
        Map<String, String> actual = new TablesContext(Collections.singletonList(tableSegment), DatabaseTypeFactory.get("MySQL"))
                .findTableNamesByColumnProjection(Collections.singletonList(columnProjection), mock(ShardingSphereSchema.class));
        assertFalse(actual.isEmpty());
        assertThat(actual.get("col"), is("table_1"));
    }
    
    @Test
    void assertFindTableNameWhenColumnProjectionOwnerPresent() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        ColumnProjection columnProjection = new ColumnProjection("table_1", "col", "cl");
        Map<String, String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeFactory.get("MySQL"))
                .findTableNamesByColumnProjection(Collections.singletonList(columnProjection), mock(ShardingSphereSchema.class));
        assertFalse(actual.isEmpty());
        assertThat(actual.get("table_1.col"), is("table_1"));
    }
    
    @Test
    void assertFindTableNameWhenColumnProjectionOwnerAbsent() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        ColumnProjection columnProjection = new ColumnProjection(null, "col", "cl");
        Map<String, String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeFactory.get("MySQL"))
                .findTableNamesByColumnProjection(Collections.singletonList(columnProjection), mock(ShardingSphereSchema.class));
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertFindTableNameWhenColumnProjectionOwnerAbsentAndSchemaMetaDataContainsColumn() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getAllColumnNames("table_1")).thenReturn(Collections.singletonList("col"));
        ColumnProjection columnProjection = new ColumnProjection(null, "col", "cl");
        Map<String, String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeFactory.get("MySQL"))
                .findTableNamesByColumnProjection(Collections.singletonList(columnProjection), schema);
        assertFalse(actual.isEmpty());
        assertThat(actual.get("col"), is("table_1"));
    }
    
    @Test
    void assertFindTableNameWhenColumnProjectionOwnerAbsentAndSchemaMetaDataContainsColumnInUpperCase() {
        SimpleTableSegment tableSegment1 = createTableSegment("TABLE_1", "TBL_1");
        SimpleTableSegment tableSegment2 = createTableSegment("TABLE_2", "TBL_2");
        ShardingSphereTable table = new ShardingSphereTable("TABLE_1", Collections.singletonList(
                new ShardingSphereColumn("COL", 0, false, false, true, true, false)), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema(Stream.of(table).collect(Collectors.toMap(ShardingSphereTable::getName, value -> value)), Collections.emptyMap());
        ColumnProjection columnProjection = new ColumnProjection(null, "COL", "CL");
        Map<String, String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeFactory.get("MySQL"))
                .findTableNamesByColumnProjection(Collections.singletonList(columnProjection), schema);
        assertFalse(actual.isEmpty());
        assertThat(actual.get("col"), is("TABLE_1"));
    }
    
    private SimpleTableSegment createTableSegment(final String tableName, final String alias) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(tableName)));
        AliasSegment aliasSegment = new AliasSegment(0, 0, new IdentifierValue(alias));
        result.setAlias(aliasSegment);
        return result;
    }
    
    private ColumnSegment createColumnSegment(final String owner, final String name) {
        ColumnSegment result = new ColumnSegment(0, 0, new IdentifierValue(name));
        if (null != owner) {
            result.setOwner(new OwnerSegment(0, 0, new IdentifierValue(owner)));
        }
        return result;
    }
    
    @Test
    void assertGetSchemaNameWithSameSchemaAndSameTable() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        tableSegment1.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        SimpleTableSegment tableSegment2 = createTableSegment("table_1", "tbl_1");
        tableSegment2.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        TablesContext tablesContext = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeFactory.get("MySQL"));
        assertTrue(tablesContext.getDatabaseName().isPresent());
        assertThat(tablesContext.getDatabaseName().get(), is("sharding_db_1"));
    }
    
    @Test
    void assertGetSchemaNameWithSameSchemaAndDifferentTable() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        tableSegment1.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        tableSegment2.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        TablesContext tablesContext = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeFactory.get("MySQL"));
        assertTrue(tablesContext.getDatabaseName().isPresent());
        assertThat(tablesContext.getDatabaseName().get(), is("sharding_db_1"));
    }
    
    @Test
    void assertGetSchemaNameWithDifferentSchemaAndSameTable() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        tableSegment1.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        SimpleTableSegment tableSegment2 = createTableSegment("table_1", "tbl_1");
        tableSegment2.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_2")));
        assertThrows(IllegalStateException.class, () -> new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeFactory.get("MySQL")).getDatabaseName());
    }
    
    @Test
    void assertGetSchemaNameWithDifferentSchemaAndDifferentTable() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        tableSegment1.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        tableSegment2.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_2")));
        assertThrows(IllegalStateException.class, () -> new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeFactory.get("MySQL")).getDatabaseName());
    }
    
    @Test
    void assertGetSchemaName() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        tableSegment1.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        tableSegment2.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        TablesContext tablesContext = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeFactory.get("MySQL"));
        assertTrue(tablesContext.getSchemaName().isPresent());
        assertThat(tablesContext.getSchemaName().get(), is("sharding_db_1"));
    }
}
