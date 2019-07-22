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

package org.apache.shardingsphere.core.optimize.api.segment;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TablesTest {
    
    @Test
    public void assertIsEmpty() {
        Tables tables = new Tables(new SelectStatement());
        assertTrue(tables.isEmpty());
    }
    
    @Test
    public void assertIsNotEmpty() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table", "tbl"));
        Tables tables = new Tables(selectStatement);
        assertFalse(tables.isEmpty());
    }
    
    @Test
    public void assertIsSingleTable() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table", "tbl"));
        Tables tables = new Tables(selectStatement);
        assertTrue(tables.isSingleTable());
    }
    
    @Test
    public void assertIsSingleTableWithCaseSensitiveNames() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table", "tbl"));
        selectStatement.getAllSQLSegments().add(createTableSegment("Table", null));
        Tables tables = new Tables(selectStatement);
        assertTrue(tables.isSingleTable());
    }
    
    @Test
    public void assertIsNotSingleTable() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table_1", "tbl_1"));
        selectStatement.getAllSQLSegments().add(createTableSegment("table_2", "tbl_2"));
        Tables tables = new Tables(selectStatement);
        assertFalse(tables.isSingleTable());
    }
    
    @Test
    public void assertGetSingleTableName() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table", "tbl"));
        Tables tables = new Tables(selectStatement);
        assertThat(tables.getSingleTableName(), is("table"));
    }
    
    @Test
    public void assertGetTableNames() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table_1", "tbl_1"));
        selectStatement.getAllSQLSegments().add(createTableSegment("table_2", "tbl_2"));
        Tables tables = new Tables(selectStatement);
        assertThat(tables.getTableNames(), CoreMatchers.<Collection<String>>is(Sets.newHashSet("table_1", "table_2")));
    }
    
    @Test
    public void assertFindTableWithName() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table_1", "tbl_1"));
        selectStatement.getAllSQLSegments().add(createTableSegment("table_2", "tbl_2"));
        Tables tables = new Tables(selectStatement);
        Optional<Table> table = tables.find("table_1");
        assertTrue(table.isPresent());
        assertThat(table.get().getName(), is("table_1"));
        assertThat(table.get().getAlias().orNull(), is("tbl_1"));
    }
    
    @Test
    public void assertFindTableWithAlias() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table_1", "tbl_1"));
        selectStatement.getAllSQLSegments().add(createTableSegment("table_2", "tbl_2"));
        Tables tables = new Tables(selectStatement);
        Optional<Table> table = tables.find("tbl_1");
        assertTrue(table.isPresent());
        assertThat(table.get().getName(), is("table_1"));
        assertThat(table.get().getAlias().orNull(), is("tbl_1"));
    }
    
    @Test
    public void assertNotFoundTable() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(createTableSegment("table_1", "tbl_1"));
        selectStatement.getAllSQLSegments().add(createTableSegment("table_2", "tbl_2"));
        Tables tables = new Tables(selectStatement);
        Optional<Table> table = tables.find("table_3");
        assertFalse(table.isPresent());
    }
    
    private TableSegment createTableSegment(final String tableName, final String alias) {
        TableSegment result = new TableSegment(0, 0, tableName);
        result.setAlias(alias);
        return result;
    }
}
