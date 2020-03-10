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

import com.google.common.collect.Sets;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TablesContextTest {
    
    @Test
    public void assertGetTableNames() {
        TablesContext tablesContext = new TablesContext(Arrays.asList(createTableSegment("table_1", "tbl_1"), createTableSegment("table_2", "tbl_2")));
        assertThat(tablesContext.getTableNames(), is(Sets.newHashSet("table_1", "table_2")));
    }
    
    @Test
    public void assertInstanceCreatedWhenNoExceptionThrown() {
        SimpleTableSegment tableSegment = new SimpleTableSegment(0, 10, new IdentifierValue("tbl"));
        tableSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("schema")));
        new TablesContext(Collections.singletonList(tableSegment));
    }
    
    @Test
    public void assertFindTableNameWhenSingleTable() {
        SimpleTableSegment tableSegment = createTableSegment("table_1", "tbl_1");
        PredicateSegment predicateSegment = createPredicateSegment(createColumnSegment());
        Optional<String> actual = new TablesContext(Collections.singletonList(tableSegment)).findTableName(predicateSegment, mock(RelationMetas.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("table_1"));
    }
    
    @Test
    public void assertFindTableNameWhenColumnSegmentOwnerPresent() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        ColumnSegment columnSegment = createColumnSegment();
        columnSegment.setOwner(new OwnerSegment(0, 10, new IdentifierValue("table_1")));
        PredicateSegment predicateSegment = createPredicateSegment(columnSegment);
        Optional<String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2)).findTableName(predicateSegment, mock(RelationMetas.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("table_1"));
    }
    
    @Test
    public void assertFindTableNameWhenColumnSegmentOwnerAbsent() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        PredicateSegment predicateSegment = createPredicateSegment(createColumnSegment());
        Optional<String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2)).findTableName(predicateSegment, mock(RelationMetas.class));
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertFindTableNameWhenColumnSegmentOwnerAbsentAndRelationMetasContainsColumn() {
        SimpleTableSegment tableSegment1 = createTableSegment("table_1", "tbl_1");
        SimpleTableSegment tableSegment2 = createTableSegment("table_2", "tbl_2");
        PredicateSegment predicateSegment = createPredicateSegment(createColumnSegment());
        RelationMetas relationMetas = mock(RelationMetas.class);
        when(relationMetas.containsColumn(anyString(), anyString())).thenReturn(true);
        Optional<String> actual = new TablesContext(Arrays.asList(tableSegment1, tableSegment2)).findTableName(predicateSegment, relationMetas);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("table_1"));
    }
    
    private SimpleTableSegment createTableSegment(final String tableName, final String alias) {
        SimpleTableSegment result = new SimpleTableSegment(0, 0, new IdentifierValue(tableName));
        AliasSegment aliasSegment = new AliasSegment(0, 0, new IdentifierValue(alias));
        result.setAlias(aliasSegment);
        return result;
    }
    
    private PredicateSegment createPredicateSegment(final ColumnSegment columnSegment) {
        return new PredicateSegment(0, 0, columnSegment, mock(PredicateRightValue.class));
    }
    
    private ColumnSegment createColumnSegment() {
        return new ColumnSegment(0, 0, new IdentifierValue("col"));
    }
}
