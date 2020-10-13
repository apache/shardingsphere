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

package org.apache.shardingsphere.sharding.route.engine.validator.dml;

import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.model.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.impl.ShardingInsertStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingInsertStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateInsertModifyMultiTables() {
        SQLStatementContext<InsertStatement> sqlStatementContext = new InsertStatementContext(
                new PhysicalSchemaMetaData(Collections.emptyMap()), Collections.singletonList(1), createInsertStatement(true));
        new ShardingInsertStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereMetaData.class));
    }
    
    @Test
    public void assertValidateOnDuplicateKeyWithoutShardingKey() {
        when(shardingRule.isShardingColumn("id", "user")).thenReturn(false);
        SQLStatementContext<InsertStatement> sqlStatementContext = new InsertStatementContext(
                new PhysicalSchemaMetaData(Collections.emptyMap()), Collections.singletonList(1), createInsertStatement(false));
        new ShardingInsertStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereMetaData.class));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateOnDuplicateKeyWithShardingKey() {
        when(shardingRule.isShardingColumn("id", "user")).thenReturn(true);
        SQLStatementContext<InsertStatement> sqlStatementContext = new InsertStatementContext(
                new PhysicalSchemaMetaData(Collections.emptyMap()), Collections.singletonList(1), createInsertStatement(false));
        new ShardingInsertStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereMetaData.class));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateInsertSelectWithoutKeyGenerateColumn() {
        when(shardingRule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(shardingRule.isGenerateKeyColumn("id", "user")).thenReturn(false);
        SQLStatementContext<InsertStatement> sqlStatementContext = new InsertStatementContext(
                new PhysicalSchemaMetaData(Collections.emptyMap()), Collections.singletonList(1), createInsertSelectStatement());
        sqlStatementContext.getTablesContext().getTables().addAll(createSingleTablesContext().getTables());
        new ShardingInsertStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereMetaData.class));
    }
    
    @Test
    public void assertValidateInsertSelectWithKeyGenerateColumn() {
        when(shardingRule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(shardingRule.isGenerateKeyColumn("id", "user")).thenReturn(true);
        SQLStatementContext<InsertStatement> sqlStatementContext = new InsertStatementContext(
                new PhysicalSchemaMetaData(Collections.emptyMap()), Collections.singletonList(1), createInsertSelectStatement());
        sqlStatementContext.getTablesContext().getTables().addAll(createSingleTablesContext().getTables());
        new ShardingInsertStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereMetaData.class));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateInsertSelectWithoutBindingTables() {
        when(shardingRule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(shardingRule.isGenerateKeyColumn("id", "user")).thenReturn(true);
        TablesContext multiTablesContext = createMultiTablesContext();
        when(shardingRule.isAllBindingTables(multiTablesContext.getTableNames())).thenReturn(false);
        SQLStatementContext<InsertStatement> sqlStatementContext = new InsertStatementContext(
                new PhysicalSchemaMetaData(Collections.emptyMap()), Collections.singletonList(1), createInsertSelectStatement());
        sqlStatementContext.getTablesContext().getTables().addAll(multiTablesContext.getTables());
        new ShardingInsertStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereMetaData.class));
    }
    
    @Test
    public void assertValidateInsertSelectWithBindingTables() {
        when(shardingRule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(shardingRule.isGenerateKeyColumn("id", "user")).thenReturn(true);
        TablesContext multiTablesContext = createMultiTablesContext();
        when(shardingRule.isAllBindingTables(multiTablesContext.getTableNames())).thenReturn(true);
        SQLStatementContext<InsertStatement> sqlStatementContext = new InsertStatementContext(
                new PhysicalSchemaMetaData(Collections.emptyMap()), Collections.singletonList(1), createInsertSelectStatement());
        sqlStatementContext.getTablesContext().getTables().addAll(multiTablesContext.getTables());
        new ShardingInsertStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereMetaData.class));
    }
    
    private InsertStatement createInsertStatement(final boolean includeMultiTable) {
        MySQLInsertStatement result = new MySQLInsertStatement();
        result.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("user")));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("id"));
        if (includeMultiTable) {
            columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("t")));
        }
        AssignmentSegment assignmentSegment = new AssignmentSegment(0, 0, columnSegment, new ParameterMarkerExpressionSegment(0, 0, 1));
        result.setOnDuplicateKeyColumns(new OnDuplicateKeyColumnsSegment(0, 0, Collections.singletonList(assignmentSegment)));
        Collection<ColumnSegment> columns = new LinkedList<>();
        columns.add(columnSegment);
        result.setInsertColumns(new InsertColumnsSegment(0, 0, columns));
        return result;
    }

    private InsertStatement createInsertSelectStatement() {
        InsertStatement result = createInsertStatement(false);
        SelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        result.setInsertSelect(new SubquerySegment(0, 0, selectStatement));
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
