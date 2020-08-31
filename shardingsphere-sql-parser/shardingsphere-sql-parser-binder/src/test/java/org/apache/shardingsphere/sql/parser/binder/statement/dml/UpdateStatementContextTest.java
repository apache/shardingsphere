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

package org.apache.shardingsphere.sql.parser.binder.statement.dml;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class UpdateStatementContextTest {
    
    @Mock
    private WhereSegment whereSegment;
    
    @Mock
    private AndPredicate andPredicate;
    
    @Mock
    private PredicateSegment predicateSegment;
    
    @Mock
    private ColumnSegment columnSegment;
    
    @Test
    public void assertNewInstance() {
        when(columnSegment.getOwner()).thenReturn(Optional.of(new OwnerSegment(0, 0, new IdentifierValue("tbl_2"))));
        when(predicateSegment.getColumn()).thenReturn(columnSegment);
        when(andPredicate.getPredicates()).thenReturn(Collections.singletonList(predicateSegment));
        when(whereSegment.getAndPredicates()).thenReturn(Lists.newLinkedList(Arrays.asList(andPredicate)));
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(0, 0, new IdentifierValue("tbl_1"));
        UpdateStatement updateStatement = new UpdateStatement();
        updateStatement.setWhere(whereSegment);
        updateStatement.getTables().add(simpleTableSegment);
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertTrue(actual.toString().startsWith(String.format("%s(super", UpdateStatementContext.class.getSimpleName())));
        assertThat(actual.getTablesContext().getTables(), is(Collections.singletonList(simpleTableSegment)));
        assertThat(actual.getWhere(), is(Optional.of(whereSegment)));
        assertThat(actual.getAllTables().stream().map(a -> a.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Arrays.asList("tbl_1", "tbl_2")));
    }
}
