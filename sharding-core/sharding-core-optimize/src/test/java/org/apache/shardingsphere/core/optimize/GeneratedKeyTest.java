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

package org.apache.shardingsphere.core.optimize;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.old.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.old.parser.context.table.Table;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLIgnoreExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLTextExpression;
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
    
    @Before
    public void setUp() {
        insertStatement.getTables().add(new Table("tbl", null));
        insertStatement.getColumnNames().add("id");
    }
    
    @Test
    public void assertGetGenerateKeyWithoutGenerateKeyColumnConfiguration() {
        when(shardingRule.findGenerateKeyColumnName("tbl")).thenReturn(Optional.<String>absent());
        assertFalse(GeneratedKey.getGenerateKey(shardingRule, Collections.<Object>singletonList(1), insertStatement).isPresent());
    }
    
    @Test
    public void assertGetGenerateKeyWhenCreateWithGenerateKeyColumnConfiguration() {
        insertStatement.getValues().add(new InsertValue(Collections.<SQLExpression>emptyList()));
        when(shardingRule.findGenerateKeyColumnName("tbl")).thenReturn(Optional.of("id1"));
        Optional<GeneratedKey> actual = GeneratedKey.getGenerateKey(shardingRule, Collections.<Object>singletonList(1), insertStatement);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getGeneratedKeys().size(), is(1));
    }
    
    @Test
    public void assertGetGenerateKeyWhenFind() {
        insertStatement.getValues().add(new InsertValue(Collections.<SQLExpression>singletonList(new SQLPlaceholderExpression(0))));
        insertStatement.getValues().add(new InsertValue(Collections.<SQLExpression>singletonList(new SQLNumberExpression(100))));
        insertStatement.getValues().add(new InsertValue(Collections.<SQLExpression>singletonList(new SQLTextExpression("value"))));
        insertStatement.getValues().add(new InsertValue(Collections.<SQLExpression>singletonList(new SQLIgnoreExpression("ignored value"))));
        when(shardingRule.findGenerateKeyColumnName("tbl")).thenReturn(Optional.of("id"));
        Optional<GeneratedKey> actual = GeneratedKey.getGenerateKey(shardingRule, Collections.<Object>singletonList(1), insertStatement);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getGeneratedKeys().size(), is(3));
        assertThat(actual.get().getGeneratedKeys().get(0), is((Comparable) 1));
        assertThat(actual.get().getGeneratedKeys().get(1), is((Comparable) 100));
        assertThat(actual.get().getGeneratedKeys().get(2), is((Comparable) "value"));
        assertTrue(GeneratedKey.getGenerateKey(shardingRule, Collections.<Object>singletonList(1), insertStatement).isPresent());
    }
}
