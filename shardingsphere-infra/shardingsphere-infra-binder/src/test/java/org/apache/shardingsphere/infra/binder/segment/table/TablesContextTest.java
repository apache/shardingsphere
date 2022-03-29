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

import com.google.common.collect.Sets;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TablesContextTest {
    
    @Test
    public void assertGetTableNames() {
        TablesContext tablesContext = new TablesContext(Arrays.asList(createTableSegment("table_1", "tbl_1"), 
                createTableSegment("table_2", "tbl_2")), DatabaseTypeRegistry.getDefaultDatabaseType());
        assertThat(tablesContext.getTableNames(), is(Sets.newHashSet("table_1", "table_2")));
    }
    
    @Test
    public void assertInstanceCreatedWhenNoExceptionThrown() {
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(0, 10, new IdentifierValue("tbl")));
        tableSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("schema")));
        new TablesContext(Collections.singleton(tableSegment), DatabaseTypeRegistry.getDefaultDatabaseType());
        // TODO add assertion
    }
    
    @Test
    public void assertFindTableNameWhenSingleTable() {
        SimpleTableSegment tableSegment = createTableSegment("table_1", "tbl_1");
        ColumnSegment columnSegment = createColumnSegment(null, "col");
        Map<String, String> actual = new TablesContext(Collections.singletonList(tableSegment), DatabaseTypeRegistry.getDefaultDatabaseType())
                .findTableNamesByColumnSegment(Collections.singletonList(columnSegment), mock(ShardingSphereSchema.class));
        assertFalse(actual.isEmpty());
        assertThat(actual.get("col"), is("table_1"));
    }
    
    @Test
    public void assertFindTableNameWhenColumnSegmentOwnerPresent() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        ColumnSegment columnSegment = createColumnSegment("table_1", "col");
        Map<String, String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeRegistry.getDefaultDatabaseType())
                .findTableNamesByColumnSegment(Collections.singletonList(columnSegment), mock(ShardingSphereSchema.class));
        assertFalse(actual.isEmpty());
        assertThat(actual.get("table_1.col"), is("table_1"));
    }
    
    @Test
    public void assertFindTableNameWhenColumnSegmentOwnerAbsent() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        ColumnSegment columnSegment = createColumnSegment(null, "col");
        Map<String, String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeRegistry.getDefaultDatabaseType())
                .findTableNamesByColumnSegment(Collections.singletonList(columnSegment), mock(ShardingSphereSchema.class));
        assertTrue(actual.isEmpty());
    }
    
    @Test
    public void assertFindTableNameWhenColumnSegmentOwnerAbsentAndSchemaMetaDataContainsColumn() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getAllColumnNames("table_1")).thenReturn(Collections.singletonList("col"));
        ColumnSegment columnSegment = createColumnSegment(null, "col");
        Map<String, String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), 
                DatabaseTypeRegistry.getDefaultDatabaseType()).findTableNamesByColumnSegment(Collections.singletonList(columnSegment), schema);
        assertFalse(actual.isEmpty());
        assertThat(actual.get("col"), is("table_1"));
    }
    
    @Test
    public void assertFindTableNameWhenColumnSegmentOwnerAbsentAndSchemaMetaDataContainsColumnInUpperCase() {
        SimpleTableSegment tableSegment1 = createTableSegment("TABLE_1", "TBL_1");
        SimpleTableSegment tableSegment2 = createTableSegment("TABLE_2", "TBL_2");
        TableMetaData tableMetaData = new TableMetaData("TABLE_1", 
                Arrays.asList(new ColumnMetaData("COL", 0, false, false, true)), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema(Arrays.asList(tableMetaData).stream().collect(Collectors.toMap(TableMetaData::getName, v -> v)));
        ColumnSegment columnSegment = createColumnSegment(null, "COL");
        Map<String, String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), 
                DatabaseTypeRegistry.getDefaultDatabaseType()).findTableNamesByColumnSegment(Collections.singletonList(columnSegment), schema);
        assertFalse(actual.isEmpty());
        assertThat(actual.get("col"), is("TABLE_1"));
    }

    @Test
    public void assertFindTableNameWhenColumnProjectionWhenSingleTable() {
        SimpleTableSegment tableSegment = createTableSegment("table_1", "tbl_1");
        ColumnProjection columnProjection = createColumnProjection(null, "col", "cl");
        Map<String, String> actual = new TablesContext(Collections.singletonList(tableSegment), DatabaseTypeRegistry.getDefaultDatabaseType())
                .findTableNamesByColumnProjection(Collections.singletonList(columnProjection), mock(ShardingSphereSchema.class));
        assertFalse(actual.isEmpty());
        assertThat(actual.get("col"), is("table_1"));
    }

    @Test
    public void assertFindTableNameWhenColumnProjectionOwnerPresent() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        ColumnProjection columnProjection = createColumnProjection("table_1", "col", "cl");
        Map<String, String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeRegistry.getDefaultDatabaseType())
                .findTableNamesByColumnProjection(Collections.singletonList(columnProjection), mock(ShardingSphereSchema.class));
        assertFalse(actual.isEmpty());
        assertThat(actual.get("table_1.col"), is("table_1"));
    }

    @Test
    public void assertFindTableNameWhenColumnProjectionOwnerAbsent() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        ColumnProjection columnProjection = createColumnProjection(null, "col", "cl");
        Map<String, String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeRegistry.getDefaultDatabaseType())
                .findTableNamesByColumnProjection(Collections.singletonList(columnProjection), mock(ShardingSphereSchema.class));
        assertTrue(actual.isEmpty());
    }

    @Test
    public void assertFindTableNameWhenColumnProjectionOwnerAbsentAndSchemaMetaDataContainsColumn() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getAllColumnNames("table_1")).thenReturn(Collections.singletonList("col"));
        ColumnProjection columnProjection = createColumnProjection(null, "col", "cl");
        Map<String, String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeRegistry.getDefaultDatabaseType())
                .findTableNamesByColumnProjection(Collections.singletonList(columnProjection), schema);
        assertFalse(actual.isEmpty());
        assertThat(actual.get("col"), is("table_1"));
    }

    @Test
    public void assertFindTableNameWhenColumnProjectionOwnerAbsentAndSchemaMetaDataContainsColumnInUpperCase() {
        SimpleTableSegment tableSegment1 = createTableSegment("TABLE_1", "TBL_1");
        SimpleTableSegment tableSegment2 = createTableSegment("TABLE_2", "TBL_2");
        TableMetaData tableMetaData = new TableMetaData("TABLE_1",
                Arrays.asList(new ColumnMetaData("COL", 0, false, false, true)),
                Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema(Arrays.asList(tableMetaData).stream().collect(Collectors.toMap(TableMetaData::getName, v -> v)));
        ColumnProjection columnProjection = createColumnProjection(null, "COL", "CL");
        Map<String, String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeRegistry.getDefaultDatabaseType())
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

    private ColumnProjection createColumnProjection(final String owner, final String name, final String alias) {
        ColumnProjection result = new ColumnProjection(owner, name, alias);
        return result;
    }
    
    @Test
    public void assertGetSchemaNameWithSameSchemaAndSameTable() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        tableSegment1.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        SimpleTableSegment tableSegment2 = createTableSegment("table_1", "tbl_1");
        tableSegment2.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        TablesContext tablesContext = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeRegistry.getDefaultDatabaseType());
        assertTrue(tablesContext.getDatabaseName().isPresent());
        assertThat(tablesContext.getDatabaseName().get(), is("sharding_db_1"));
    }
    
    @Test
    public void assertGetSchemaNameWithSameSchemaAndDifferentTable() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        tableSegment1.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        tableSegment2.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        TablesContext tablesContext = new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeRegistry.getDefaultDatabaseType());
        assertTrue(tablesContext.getDatabaseName().isPresent());
        assertThat(tablesContext.getDatabaseName().get(), is("sharding_db_1"));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGetSchemaNameWithDifferentSchemaAndSameTable() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        tableSegment1.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        SimpleTableSegment tableSegment2 = createTableSegment("table_1", "tbl_1");
        tableSegment2.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_2")));
        new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeRegistry.getDefaultDatabaseType()).getDatabaseName();
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGetSchemaNameWithDifferentSchemaAndDifferentTable() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        tableSegment1.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        tableSegment2.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_2")));
        new TablesContext(Arrays.asList(tableSegment1, tableSegment2), DatabaseTypeRegistry.getDefaultDatabaseType()).getDatabaseName();
    }
}
