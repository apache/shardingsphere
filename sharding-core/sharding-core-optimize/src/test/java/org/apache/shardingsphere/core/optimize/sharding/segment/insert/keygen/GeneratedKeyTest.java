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

package org.apache.shardingsphere.core.optimize.sharding.segment.insert.keygen;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.GeneratedKey;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.ShardingInsertColumns;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class GeneratedKeyTest {
    
    private final InsertStatement insertStatement = new InsertStatement();
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private ShardingInsertColumns insertColumns;
    
    @Before
    public void setUp() {
        insertStatement.setTable(new TableSegment(0, 0, "tbl"));
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "id"));
        when(insertColumns.getRegularColumnNames()).thenReturn(Collections.singletonList("id"));
    }
    
    @Test
    public void assertGetGenerateKeyWithoutGenerateKeyColumnConfiguration() {
        when(shardingRule.findGenerateKeyColumnName("tbl")).thenReturn(Optional.<String>absent());
        assertFalse(GeneratedKey.getGenerateKey(shardingRule, Collections.<Object>singletonList(1), insertStatement, insertColumns).isPresent());
    }
    
    @Test
    public void assertGetGenerateKeyWhenCreateWithGenerateKeyColumnConfiguration() {
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.<ExpressionSegment>singletonList(new LiteralExpressionSegment(0, 0, 1))));
        when(shardingRule.findGenerateKeyColumnName("tbl")).thenReturn(Optional.of("id1"));
        Optional<GeneratedKey> actual = GeneratedKey.getGenerateKey(shardingRule, Collections.<Object>singletonList(1), insertStatement, insertColumns);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getGeneratedValues().size(), is(1));
    }
    
    @Test
    public void assertGetGenerateKeyWhenFind() {
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.<ExpressionSegment>singletonList(new ParameterMarkerExpressionSegment(1, 2, 0))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.<ExpressionSegment>singletonList(new LiteralExpressionSegment(1, 2, 100))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.<ExpressionSegment>singletonList(new LiteralExpressionSegment(1, 2, "value"))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.<ExpressionSegment>singletonList(new CommonExpressionSegment(1, 2, "ignored value"))));
        when(shardingRule.findGenerateKeyColumnName("tbl")).thenReturn(Optional.of("id"));
        Optional<GeneratedKey> actual = GeneratedKey.getGenerateKey(shardingRule, Collections.<Object>singletonList(1), insertStatement, insertColumns);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getGeneratedValues().size(), is(3));
        assertThat(actual.get().getGeneratedValues().get(0), is((Comparable) 1));
        assertThat(actual.get().getGeneratedValues().get(1), is((Comparable) 100));
        assertThat(actual.get().getGeneratedValues().get(2), is((Comparable) "value"));
        assertTrue(GeneratedKey.getGenerateKey(shardingRule, Collections.<Object>singletonList(1), insertStatement, insertColumns).isPresent());
    }
}
