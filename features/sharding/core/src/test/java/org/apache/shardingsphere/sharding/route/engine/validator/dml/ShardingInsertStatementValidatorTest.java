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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.DuplicateInsertDataRecordException;
import org.apache.shardingsphere.sharding.exception.syntax.DMLWithMultipleShardingTablesException;
import org.apache.shardingsphere.sharding.exception.syntax.InsertSelectTableViolationException;
import org.apache.shardingsphere.sharding.exception.syntax.MissingGenerateKeyColumnWithInsertSelectException;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedUpdatingShardingValueException;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.impl.ShardingInsertStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
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
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingInsertStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private RouteContext routeContext;
    
    @Mock
    private ShardingConditions shardingConditions;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Test
    void assertPreValidateWhenInsertMultiTables() {
        SQLStatementContext sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertStatement());
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        when(shardingRule.isAllShardingTables(tableNames)).thenReturn(false);
        when(shardingRule.containsShardingTable(tableNames)).thenReturn(true);
        when(schema.containsTable("user")).thenReturn(true);
        when(database.getSchema(any())).thenReturn(schema);
        when(database.getName()).thenReturn("sharding_db");
        when(database.getRuleMetaData().findRules(TableContainedRule.class)).thenReturn(Collections.emptyList());
        assertThrows(DMLWithMultipleShardingTablesException.class, () -> new ShardingInsertStatementValidator(shardingConditions).preValidate(shardingRule,
                sqlStatementContext, Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    private InsertStatementContext createInsertStatementContext(final List<Object> params, final InsertStatement insertStatement) {
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
        return new InsertStatementContext(metaData, params, insertStatement, DefaultDatabase.LOGIC_NAME);
    }
    
    @Test
    void assertPreValidateWhenInsertSelectWithoutKeyGenerateColumn() {
        when(shardingRule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(shardingRule.isGenerateKeyColumn("id", "user")).thenReturn(false);
        SQLStatementContext sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertSelectStatement());
        sqlStatementContext.getTablesContext().getTableNames().addAll(createSingleTablesContext().getTableNames());
        when(schema.containsTable("user")).thenReturn(true);
        when(database.getSchema(any())).thenReturn(schema);
        when(database.getName()).thenReturn("sharding_db");
        assertThrows(MissingGenerateKeyColumnWithInsertSelectException.class, () -> new ShardingInsertStatementValidator(shardingConditions).preValidate(shardingRule,
                sqlStatementContext, Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPreValidateWhenInsertSelectWithKeyGenerateColumn() {
        when(shardingRule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(shardingRule.isGenerateKeyColumn("id", "user")).thenReturn(true);
        SQLStatementContext sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertSelectStatement());
        sqlStatementContext.getTablesContext().getTableNames().addAll(createSingleTablesContext().getTableNames());
        when(schema.containsTable("user")).thenReturn(true);
        when(database.getSchema(any())).thenReturn(schema);
        when(database.getName()).thenReturn("sharding_db");
        assertDoesNotThrow(() -> new ShardingInsertStatementValidator(shardingConditions).preValidate(
                shardingRule, sqlStatementContext, Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPreValidateWhenInsertSelectWithoutBindingTables() {
        when(shardingRule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(shardingRule.isGenerateKeyColumn("id", "user")).thenReturn(true);
        TablesContext multiTablesContext = createMultiTablesContext();
        when(shardingRule.isAllBindingTables(multiTablesContext.getTableNames())).thenReturn(false);
        when(shardingRule.containsShardingTable(multiTablesContext.getTableNames())).thenReturn(true);
        SQLStatementContext sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertSelectStatement());
        sqlStatementContext.getTablesContext().getTableNames().addAll(multiTablesContext.getTableNames());
        when(schema.containsTable("user")).thenReturn(true);
        when(schema.containsTable("order")).thenReturn(true);
        when(database.getSchema(any())).thenReturn(schema);
        when(database.getName()).thenReturn("sharding_db");
        assertThrows(InsertSelectTableViolationException.class, () -> new ShardingInsertStatementValidator(shardingConditions).preValidate(
                shardingRule, sqlStatementContext, Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPreValidateWhenInsertSelectWithBindingTables() {
        when(shardingRule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(shardingRule.isGenerateKeyColumn("id", "user")).thenReturn(true);
        TablesContext multiTablesContext = createMultiTablesContext();
        SQLStatementContext sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertSelectStatement());
        sqlStatementContext.getTablesContext().getTableNames().addAll(multiTablesContext.getTableNames());
        when(schema.containsTable("user")).thenReturn(true);
        when(schema.containsTable("order")).thenReturn(true);
        when(database.getSchema(any())).thenReturn(schema);
        when(database.getName()).thenReturn("sharding_db");
        assertDoesNotThrow(() -> new ShardingInsertStatementValidator(shardingConditions).preValidate(
                shardingRule, sqlStatementContext, Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPostValidateWhenInsertWithSingleRouting() {
        SQLStatementContext sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertStatement());
        when(routeContext.isSingleRouting()).thenReturn(true);
        assertDoesNotThrow(() -> new ShardingInsertStatementValidator(shardingConditions).postValidate(shardingRule, sqlStatementContext, new HintValueContext(),
                Collections.emptyList(), database, mock(ConfigurationProperties.class), routeContext));
    }
    
    @Test
    void assertPostValidateWhenInsertWithBroadcastTable() {
        SQLStatementContext sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertStatement());
        when(routeContext.isSingleRouting()).thenReturn(false);
        assertDoesNotThrow(() -> new ShardingInsertStatementValidator(shardingConditions).postValidate(shardingRule, sqlStatementContext, new HintValueContext(),
                Collections.emptyList(), database, mock(ConfigurationProperties.class), routeContext));
    }
    
    @Test
    void assertPostValidateWhenInsertWithRoutingToSingleDataNode() {
        SQLStatementContext sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertStatement());
        when(routeContext.isSingleRouting()).thenReturn(false);
        when(routeContext.getOriginalDataNodes()).thenReturn(getSingleRouteDataNodes());
        assertDoesNotThrow(() -> new ShardingInsertStatementValidator(shardingConditions).postValidate(shardingRule, sqlStatementContext, new HintValueContext(),
                Collections.emptyList(), database, mock(ConfigurationProperties.class), routeContext));
    }
    
    @Test
    void assertPostValidateWhenInsertWithRoutingToMultipleDataNodes() {
        SQLStatementContext sqlStatementContext = createInsertStatementContext(Collections.singletonList(1), createInsertStatement());
        when(routeContext.isSingleRouting()).thenReturn(false);
        when(routeContext.getOriginalDataNodes()).thenReturn(getMultipleRouteDataNodes());
        assertThrows(DuplicateInsertDataRecordException.class, () -> new ShardingInsertStatementValidator(shardingConditions).postValidate(shardingRule, sqlStatementContext, new HintValueContext(),
                Collections.emptyList(), database, mock(ConfigurationProperties.class), routeContext));
    }
    
    @Test
    void assertPostValidateWhenNotOnDuplicateKeyUpdateShardingColumn() {
        List<Object> params = Collections.singletonList(1);
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.isSingleRouting()).thenReturn(true);
        InsertStatementContext insertStatementContext = createInsertStatementContext(params, createInsertStatement());
        assertDoesNotThrow(() -> new ShardingInsertStatementValidator(mock(ShardingConditions.class)).postValidate(
                shardingRule, insertStatementContext, new HintValueContext(), params, database, mock(ConfigurationProperties.class), routeContext));
    }
    
    @Test
    void assertPostValidateWhenOnDuplicateKeyUpdateShardingColumnWithSameRouteContext() {
        mockShardingRuleForUpdateShardingColumn();
        List<Object> params = Collections.singletonList(1);
        InsertStatementContext insertStatementContext = createInsertStatementContext(params, createInsertStatement());
        assertDoesNotThrow(() -> new ShardingInsertStatementValidator(mock(ShardingConditions.class)).postValidate(shardingRule,
                insertStatementContext, new HintValueContext(), params, database, mock(ConfigurationProperties.class), createSingleRouteContext()));
    }
    
    @Test
    void assertPostValidateWhenOnDuplicateKeyUpdateShardingColumnWithDifferentRouteContext() {
        mockShardingRuleForUpdateShardingColumn();
        List<Object> params = Collections.singletonList(1);
        InsertStatementContext insertStatementContext = createInsertStatementContext(params, createInsertStatement());
        assertThrows(UnsupportedUpdatingShardingValueException.class, () -> new ShardingInsertStatementValidator(mock(ShardingConditions.class)).postValidate(shardingRule,
                insertStatementContext, new HintValueContext(), params, database, mock(ConfigurationProperties.class), createFullRouteContext()));
    }
    
    private void mockShardingRuleForUpdateShardingColumn() {
        TableRule tableRule = mock(TableRule.class);
        when(tableRule.getActualDataSourceNames()).thenReturn(Arrays.asList("ds_0", "ds_1"));
        when(tableRule.getActualTableNames("ds_1")).thenReturn(Collections.singletonList("user"));
        when(shardingRule.findShardingColumn("id", "user")).thenReturn(Optional.of("id"));
        when(shardingRule.getTableRule("user")).thenReturn(tableRule);
        StandardShardingStrategyConfiguration databaseStrategyConfig = mock(StandardShardingStrategyConfiguration.class);
        when(databaseStrategyConfig.getShardingColumn()).thenReturn("id");
        when(databaseStrategyConfig.getShardingAlgorithmName()).thenReturn("database_inline");
        when(shardingRule.getDatabaseShardingStrategyConfiguration(tableRule)).thenReturn(databaseStrategyConfig);
        when(shardingRule.getShardingAlgorithms()).thenReturn(Collections.singletonMap("database_inline",
                TypedSPILoader.getService(ShardingAlgorithm.class, "INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${id % 2}")))));
    }
    
    private RouteContext createSingleRouteContext() {
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singletonList(new RouteMapper("user", "user"))));
        return result;
    }
    
    private RouteContext createFullRouteContext() {
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("user", "user"))));
        result.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singletonList(new RouteMapper("user", "user"))));
        return result;
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
        AssignmentSegment assignmentSegment = new ColumnAssignmentSegment(0, 0, columnSegments, new ParameterMarkerExpressionSegment(0, 0, 0));
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
        return new TablesContext(result, DatabaseTypeFactory.get("MySQL"));
    }
    
    private TablesContext createMultiTablesContext() {
        List<SimpleTableSegment> result = new LinkedList<>();
        result.add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        result.add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("order"))));
        return new TablesContext(result, DatabaseTypeFactory.get("MySQL"));
    }
}
