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

package org.apache.shardingsphere.infra.binder.context.segment.table;

import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class TablesContextTest {
    
    @Test
    void assertGetTableNamesWithoutTableSegments() {
        assertTrue(new TablesContext(Collections.emptyList()).getTableNames().isEmpty());
    }
    
    @Test
    void assertGetTableNamesWithoutSimpleTableSegments() {
        assertTrue(new TablesContext(Collections.singleton(mock(TableSegment.class))).getTableNames().isEmpty());
    }
    
    @Test
    void assertGetTableNames() {
        TablesContext tablesContext = new TablesContext(
                Arrays.asList(createTableSegment("table_1", "tbl_1", "sharding_db_1"), createTableSegment("table_2", "tbl_2", "sharding_db_1"), createTableSegment("DUAL", "dual", "sharding_db_1")));
        assertThat(tablesContext.getTableNames(), is(new HashSet<>(Arrays.asList("table_1", "table_2"))));
    }
    
    @Test
    void assertInstanceCreatedWhenNoExceptionThrown() {
        TableNameSegment tableNameSegment = new TableNameSegment(0, 10, new IdentifierValue("tbl"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_db")));
        SimpleTableSegment tableSegment = new SimpleTableSegment(tableNameSegment);
        tableSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_db")));
        TablesContext tablesContext = new TablesContext(Collections.singleton(tableSegment));
        assertThat(tablesContext.getDatabaseName(), is(Optional.of("foo_db")));
        assertThat(tablesContext.getSchemaName(), is(Optional.of("foo_db")));
        assertThat(tablesContext.getTableNames(), is(Collections.singleton("tbl")));
    }
    
    private SimpleTableSegment createTableSegment(final String tableName, final String alias, final String databaseName) {
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue(tableName));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue(databaseName), new IdentifierValue(databaseName)));
        SimpleTableSegment result = new SimpleTableSegment(tableNameSegment);
        AliasSegment aliasSegment = new AliasSegment(0, 0, new IdentifierValue(alias));
        result.setAlias(aliasSegment);
        return result;
    }
    
    @Test
    void assertGetSchemaNameWithSameSchemaAndSameTable() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1", "sharding_db_1");
        tableSegment1.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        SimpleTableSegment tableSegment2 = createTableSegment("table_1", "tbl_1", "sharding_db_1");
        tableSegment2.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        TablesContext tablesContext = new TablesContext(Arrays.asList(tableSegment1, tableSegment2));
        assertTrue(tablesContext.getDatabaseName().isPresent());
        assertThat(tablesContext.getDatabaseName().get(), is("sharding_db_1"));
    }
    
    @Test
    void assertGetSchemaNameWithSameSchemaAndDifferentTable() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1", "sharding_db_1");
        tableSegment1.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2", "sharding_db_1");
        tableSegment2.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        TablesContext tablesContext = new TablesContext(Arrays.asList(tableSegment1, tableSegment2));
        assertTrue(tablesContext.getDatabaseName().isPresent());
        assertThat(tablesContext.getDatabaseName().get(), is("sharding_db_1"));
    }
    
    @Test
    void assertGetSchemaName() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1", "sharding_db_1");
        tableSegment1.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2", "sharding_db_1");
        tableSegment2.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db_1")));
        TablesContext tablesContext = new TablesContext(Arrays.asList(tableSegment1, tableSegment2));
        assertTrue(tablesContext.getSchemaName().isPresent());
        assertThat(tablesContext.getSchemaName().get(), is("sharding_db_1"));
    }
}
