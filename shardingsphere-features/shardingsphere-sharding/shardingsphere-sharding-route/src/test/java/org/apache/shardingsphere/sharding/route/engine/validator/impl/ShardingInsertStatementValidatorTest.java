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

package org.apache.shardingsphere.sharding.route.engine.validator.impl;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.binder.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingInsertStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Test
    public void assertValidateOnDuplicateKeyWithoutShardingKey() {
        when(shardingRule.isShardingColumn("id", "user")).thenReturn(false);
        new ShardingInsertStatementValidator().validate(shardingRule, createInsertStatement(), createSingleTablesContext(), Collections.emptyList());
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateOnDuplicateKeyWithShardingKey() {
        when(shardingRule.isShardingColumn("id", "user")).thenReturn(true);
        new ShardingInsertStatementValidator().validate(shardingRule, createInsertStatement(), createSingleTablesContext(), Collections.emptyList());
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateInsertSelectWithoutKeyGenerateColumn() {
        when(shardingRule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(shardingRule.isGenerateKeyColumn("id", "user")).thenReturn(false);
        new ShardingInsertStatementValidator().validate(shardingRule, createInsertStatement(), createSingleTablesContext(), Collections.emptyList());
    }
    
    @Test
    public void assertValidateInsertSelectWithKeyGenerateColumn() {
        when(shardingRule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(shardingRule.isGenerateKeyColumn("id", "user")).thenReturn(true);
        new ShardingInsertStatementValidator().validate(shardingRule, createInsertStatement(), createSingleTablesContext(), Collections.emptyList());
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateInsertSelectWithoutBindingTables() {
        when(shardingRule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(shardingRule.isGenerateKeyColumn("id", "user")).thenReturn(true);
        TablesContext multiTablesContext = createMultiTablesContext();
        when(shardingRule.isAllBindingTables(multiTablesContext.getTableNames())).thenReturn(false);
        new ShardingInsertStatementValidator().validate(shardingRule, createInsertStatement(), multiTablesContext, Collections.emptyList());
    }
    
    @Test
    public void assertValidateInsertSelectWithBindingTables() {
        when(shardingRule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(shardingRule.isGenerateKeyColumn("id", "user")).thenReturn(true);
        TablesContext multiTablesContext = createMultiTablesContext();
        when(shardingRule.isAllBindingTables(multiTablesContext.getTableNames())).thenReturn(true);
        new ShardingInsertStatementValidator().validate(shardingRule, createInsertStatement(), multiTablesContext, Collections.emptyList());
    }
    
    private InsertStatement createInsertStatement() {
        InsertStatement result = new InsertStatement();
        result.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("user")));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("id"));
        AssignmentSegment assignmentSegment = new AssignmentSegment(0, 0, columnSegment, new ParameterMarkerExpressionSegment(0, 0, 1));
        result.setOnDuplicateKeyColumns(new OnDuplicateKeyColumnsSegment(0, 0, Collections.singletonList(assignmentSegment)));
        Collection<ColumnSegment> columns = new LinkedList<>();
        columns.add(columnSegment);
        result.setInsertColumns(new InsertColumnsSegment(0, 0, columns));
        result.setInsertSelect(new SubquerySegment(0, 0, new SelectStatement()));
        return result;
    }
    
    private TablesContext createSingleTablesContext() {
        List<SimpleTableSegment> result = new LinkedList<>();
        result.add(new SimpleTableSegment(0, 0, new IdentifierValue("user")));
        return new TablesContext(result);
    }
    
    private TablesContext createMultiTablesContext() {
        List<SimpleTableSegment> result = new LinkedList<>();
        result.add(new SimpleTableSegment(0, 0, new IdentifierValue("user")));
        result.add(new SimpleTableSegment(0, 0, new IdentifierValue("order")));
        return new TablesContext(result);
    }
}
