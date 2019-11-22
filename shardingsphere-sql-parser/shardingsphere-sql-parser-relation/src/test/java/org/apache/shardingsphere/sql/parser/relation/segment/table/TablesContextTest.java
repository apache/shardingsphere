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

package org.apache.shardingsphere.sql.parser.relation.segment.table;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableAvailable;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TablesContextTest {
    
    @Test
    public void assertIsEmpty() {
        TablesContext tablesContext = new TablesContext(new SelectStatement());
        assertTrue(tablesContext.isEmpty());
    }
    
    @Test
    public void assertIsNotEmpty() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table", "tbl"));
        TablesContext tablesContext = new TablesContext(selectStatement);
        assertFalse(tablesContext.isEmpty());
    }
    
    @Test
    public void assertIsSingleTable() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table", "tbl"));
        TablesContext tablesContext = new TablesContext(selectStatement);
        assertTrue(tablesContext.isSingleTable());
    }
    
    @Test
    public void assertIsSingleTableWithCaseSensitiveNames() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table", "tbl"));
        selectStatement.getAllSQLSegments().add(createTableSegment("Table", null));
        TablesContext tablesContext = new TablesContext(selectStatement);
        assertTrue(tablesContext.isSingleTable());
    }
    
    @Test
    public void assertIsSingleTableWithNameConflictAlias() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table", "tbl"));
        selectStatement.getAllSQLSegments().add(createTableSegment("tbl", null));
        TablesContext tablesContext = new TablesContext(selectStatement);
        assertTrue(tablesContext.isSingleTable());
    }
    
    @Test
    public void assertIsNotSingleTable() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table_1", "tbl_1"));
        selectStatement.getAllSQLSegments().add(createTableSegment("table_2", "tbl_2"));
        TablesContext tablesContext = new TablesContext(selectStatement);
        assertFalse(tablesContext.isSingleTable());
    }
    
    @Test
    public void assertGetSingleTableName() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table", "tbl"));
        TablesContext tablesContext = new TablesContext(selectStatement);
        assertThat(tablesContext.getSingleTableName(), is("table"));
    }
    
    @Test
    public void assertGetTableNames() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table_1", "tbl_1"));
        selectStatement.getAllSQLSegments().add(createTableSegment("table_2", "tbl_2"));
        TablesContext tablesContext = new TablesContext(selectStatement);
        assertThat(tablesContext.getTableNames(), CoreMatchers.<Collection<String>>is(Sets.newHashSet("table_1", "table_2")));
    }
    
    @Test
    public void assertFindTableWithName() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table_1", "tbl_1"));
        selectStatement.getAllSQLSegments().add(createTableSegment("table_2", "tbl_2"));
        TablesContext tablesContext = new TablesContext(selectStatement);
        Optional<Table> table = tablesContext.find("table_1");
        assertTrue(table.isPresent());
        assertThat(table.get().getName(), is("table_1"));
        assertThat(table.get().getAlias().orNull(), is("tbl_1"));
    }
    
    @Test
    public void assertFindTableWithAlias() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table_1", "tbl_1"));
        selectStatement.getAllSQLSegments().add(createTableSegment("table_2", "tbl_2"));
        TablesContext tablesContext = new TablesContext(selectStatement);
        Optional<Table> table = tablesContext.find("tbl_1");
        assertTrue(table.isPresent());
        assertThat(table.get().getName(), is("table_1"));
        assertThat(table.get().getAlias().orNull(), is("tbl_1"));
    }
    
    @Test
    public void assertNotFoundTable() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table_1", "tbl_1"));
        selectStatement.getAllSQLSegments().add(createTableSegment("table_2", "tbl_2"));
        TablesContext tablesContext = new TablesContext(selectStatement);
        Optional<Table> table = tablesContext.find("table_3");
        assertFalse(table.isPresent());
    }
    
    @Test
    public void assertFindTableNameWhenSingleTable() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table_1", "tbl_1"));
        assertTrue(new TablesContext(selectStatement).findTableName(null, null).isPresent());
    }
    
    @Test
    public void assertFindTableNameWhenColumnSegmentOwnerPresent() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table_1", "tbl_1"));
        selectStatement.getAllSQLSegments().add(createTableSegment("table_2", "tbl_2"));
        TablesContext tablesContext = new TablesContext(selectStatement);
        ColumnSegment columnSegment = mock(ColumnSegment.class);
        when(columnSegment.getOwner()).thenReturn(Optional.of(new TableSegment(0, 10, "table_1")));
        assertTrue(tablesContext.findTableName(columnSegment, null).isPresent());
    }
    
    @Test
    public void assertFindTableNameWhenColumnSegmentOwnerAbsent() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table_1", "tbl_1"));
        selectStatement.getAllSQLSegments().add(createTableSegment("table_2", "tbl_2"));
        TablesContext tablesContext = new TablesContext(selectStatement);
        ColumnSegment columnSegment = mock(ColumnSegment.class);
        when(columnSegment.getOwner()).thenReturn(Optional.<TableSegment>absent());
        RelationMetas relationMetas = mock(RelationMetas.class);
        assertFalse(tablesContext.findTableName(columnSegment, relationMetas).isPresent());
    }
    
    @Test
    public void assertFindTableNameWhenColumnSegmentOwnerAbsentAndRelationMetasContainsColumn() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table_1", "tbl_1"));
        selectStatement.getAllSQLSegments().add(createTableSegment("table_2", "tbl_2"));
        ColumnSegment columnSegment = mock(ColumnSegment.class);
        when(columnSegment.getOwner()).thenReturn(Optional.<TableSegment>absent());
        when(columnSegment.getName()).thenReturn("columnName");
        RelationMetas relationMetas = mock(RelationMetas.class);
        when(relationMetas.containsColumn(anyString(), anyString())).thenReturn(true);
        assertTrue(new TablesContext(selectStatement).findTableName(columnSegment, relationMetas).isPresent());
    }
    
    @Test
    public void assertGetSchema() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table_1", "tbl_1"));
        assertFalse(new TablesContext(selectStatement).getSchema().isPresent());
    }
    
    @Test
    public void assertInstanceCreatedWhenNoExceptionThrown() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        TableSegment tableSegment = new TableSegment(0, 10, "TableSegmentName");
        SchemaSegment schemaSegment = mock(SchemaSegment.class);
        when(schemaSegment.getName()).thenReturn("SchemaSegmentName");
        tableSegment.setOwner(schemaSegment);
        when(sqlStatement.findSQLSegments(TableAvailable.class)).thenReturn(Collections.singletonList((TableAvailable) tableSegment));
        new TablesContext(sqlStatement);
    }
    
    private TableSegment createTableSegment(final String tableName, final String alias) {
        TableSegment result = new TableSegment(0, 0, tableName);
        result.setAlias(alias);
        return result;
    }
}
