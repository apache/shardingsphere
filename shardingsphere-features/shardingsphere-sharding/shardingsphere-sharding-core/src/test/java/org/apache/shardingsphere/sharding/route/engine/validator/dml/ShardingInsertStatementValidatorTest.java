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
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.impl.ShardingInsertStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
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
    
    @Mock
    private RouteContext routeContext;
    
    @Mock
    private ShardingConditions shardingConditions;
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateInsertModifyMultiTables() {
        SQLStatementContext<InsertStatement> sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertStatement());
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        when(shardingRule.isAllShardingTables(tableNames)).thenReturn(false);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(true);
        new ShardingInsertStatementValidator(shardingConditions).preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereSchema.class));
    }
    
    private InsertStatementContext createInsertStatementContext(final List<Object> parameters, final InsertStatement insertStatement) {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getSchema()).thenReturn(mock(ShardingSphereSchema.class));
        return new InsertStatementContext(Collections.singletonMap(DefaultSchema.LOGIC_NAME, metaData), parameters, insertStatement, DefaultSchema.LOGIC_NAME);
    }
    
    @Test
    public void assertValidateOnDuplicateKeyWithoutShardingKey() {
        when(shardingRule.isShardingColumn("id", "user")).thenReturn(false);
        SQLStatementContext<InsertStatement> sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertStatement());
        new ShardingInsertStatementValidator(shardingConditions).preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereSchema.class));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateOnDuplicateKeyWithShardingKey() {
        when(shardingRule.isShardingColumn("id", "user")).thenReturn(true);
        SQLStatementContext<InsertStatement> sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertStatement());
        new ShardingInsertStatementValidator(shardingConditions).preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereSchema.class));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateInsertSelectWithoutKeyGenerateColumn() {
        when(shardingRule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(shardingRule.isGenerateKeyColumn("id", "user")).thenReturn(false);
        SQLStatementContext<InsertStatement> sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertSelectStatement());
        sqlStatementContext.getTablesContext().getUniqueTables().putAll(createSingleTablesContext().getUniqueTables());
        new ShardingInsertStatementValidator(shardingConditions).preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereSchema.class));
    }
    
    @Test
    public void assertValidateInsertSelectWithKeyGenerateColumn() {
        when(shardingRule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(shardingRule.isGenerateKeyColumn("id", "user")).thenReturn(true);
        SQLStatementContext<InsertStatement> sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertSelectStatement());
        sqlStatementContext.getTablesContext().getUniqueTables().putAll(createSingleTablesContext().getUniqueTables());
        new ShardingInsertStatementValidator(shardingConditions).preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereSchema.class));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateInsertSelectWithoutBindingTables() {
        when(shardingRule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(shardingRule.isGenerateKeyColumn("id", "user")).thenReturn(true);
        TablesContext multiTablesContext = createMultiTablesContext();
        when(shardingRule.isAllBindingTables(multiTablesContext.getTableNames())).thenReturn(false);
        SQLStatementContext<InsertStatement> sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertSelectStatement());
        sqlStatementContext.getTablesContext().getUniqueTables().putAll(multiTablesContext.getUniqueTables());
        new ShardingInsertStatementValidator(shardingConditions).preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereSchema.class));
    }
    
    @Test
    public void assertValidateInsertSelectWithBindingTables() {
        when(shardingRule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(shardingRule.isGenerateKeyColumn("id", "user")).thenReturn(true);
        TablesContext multiTablesContext = createMultiTablesContext();
        when(shardingRule.isAllBindingTables(multiTablesContext.getTableNames())).thenReturn(true);
        SQLStatementContext<InsertStatement> sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertSelectStatement());
        sqlStatementContext.getTablesContext().getUniqueTables().putAll(multiTablesContext.getUniqueTables());
        new ShardingInsertStatementValidator(shardingConditions).preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereSchema.class));
    }
    
    @Test
    public void assertValidateInsertWithSingleRouting() {
        SQLStatementContext<InsertStatement> sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertStatement());
        when(routeContext.isSingleRouting()).thenReturn(true);
        new ShardingInsertStatementValidator(shardingConditions).postValidate(shardingRule, sqlStatementContext, routeContext, mock(ShardingSphereSchema.class));
    }
    
    @Test
    public void assertValidateInsertWithBroadcastTable() {
        SQLStatementContext<InsertStatement> sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertStatement());
        when(routeContext.isSingleRouting()).thenReturn(false);
        when(shardingRule.isBroadcastTable(sqlStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue())).thenReturn(true);
        new ShardingInsertStatementValidator(shardingConditions).postValidate(shardingRule, sqlStatementContext, routeContext, mock(ShardingSphereSchema.class));
    }
    
    @Test
    public void assertValidateInsertWithRoutingToSingleDataNode() {
        SQLStatementContext<InsertStatement> sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertStatement());
        when(routeContext.isSingleRouting()).thenReturn(false);
        when(shardingRule.isBroadcastTable(sqlStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue())).thenReturn(false);
        when(routeContext.getOriginalDataNodes()).thenReturn(getSingleRouteDataNodes());
        new ShardingInsertStatementValidator(shardingConditions).postValidate(shardingRule, sqlStatementContext, routeContext, mock(ShardingSphereSchema.class));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateInsertWithRoutingToMultipleDataNodes() {
        SQLStatementContext<InsertStatement> sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertStatement());
        when(routeContext.isSingleRouting()).thenReturn(false);
        when(shardingRule.isBroadcastTable(sqlStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue())).thenReturn(false);
        when(routeContext.getOriginalDataNodes()).thenReturn(getMultipleRouteDataNodes());
        new ShardingInsertStatementValidator(shardingConditions).postValidate(shardingRule, sqlStatementContext, routeContext, mock(ShardingSphereSchema.class));
    }
    
    private Collection<Collection<DataNode>> getMultipleRouteDataNodes() {
        Collection<DataNode> value1DataNodes = new LinkedList<>();
        value1DataNodes.add(new DataNode("ds_0", "user_0"));
        Collection<DataNode> value2DataNodes = new LinkedList<>();
        value2DataNodes.add(new DataNode("ds_0", "user_0"));
        value2DataNodes.add(new DataNode("ds_0", "user_1"));
        Collection<Collection<DataNode>> result = new LinkedList<>();
        result.add(value1DataNodes);
        result.add(value2DataNodes);
        return result;
    }
    
    private Collection<Collection<DataNode>> getSingleRouteDataNodes() {
        Collection<DataNode> value1DataNodes = new LinkedList<>();
        value1DataNodes.add(new DataNode("ds_0", "user_0"));
        Collection<DataNode> value2DataNodes = new LinkedList<>();
        value2DataNodes.add(new DataNode("ds_0", "user_0"));
        Collection<Collection<DataNode>> result = new LinkedList<>();
        result.add(value1DataNodes);
        result.add(value2DataNodes);
        return result;
    }
    
    private InsertStatement createInsertStatement() {
        MySQLInsertStatement result = new MySQLInsertStatement();
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("id"));
        List<ColumnSegment> columnSegments = new LinkedList<>();
        columnSegments.add(columnSegment);
        AssignmentSegment assignmentSegment = new ColumnAssignmentSegment(0, 0, columnSegments, new ParameterMarkerExpressionSegment(0, 0, 1));
        result.setOnDuplicateKeyColumns(new OnDuplicateKeyColumnsSegment(0, 0, Collections.singletonList(assignmentSegment)));
        Collection<ColumnSegment> columns = new LinkedList<>();
        columns.add(columnSegment);
        result.setInsertColumns(new InsertColumnsSegment(0, 0, columns));
        return result;
    }
    
    private InsertStatement createInsertSelectStatement() {
        InsertStatement result = createInsertStatement();
        SelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        result.setInsertSelect(new SubquerySegment(0, 0, selectStatement));
        return result;
    }
    
    private TablesContext createSingleTablesContext() {
        List<SimpleTableSegment> result = new LinkedList<>();
        result.add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        return new TablesContext(result);
    }
    
    private TablesContext createMultiTablesContext() {
        List<SimpleTableSegment> result = new LinkedList<>();
        result.add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        result.add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("order"))));
        return new TablesContext(result);
    }
}
