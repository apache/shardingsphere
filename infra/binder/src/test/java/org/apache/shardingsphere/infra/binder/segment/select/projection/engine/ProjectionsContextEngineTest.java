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

package org.apache.shardingsphere.infra.binder.segment.select.projection.engine;

import org.apache.shardingsphere.infra.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.sql.parser.sql.common.enums.NullsOrderType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectionsContextEngineTest {
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Test
    void assertProjectionsContextCreatedProperlyForMySQL() {
        assertProjectionsContextCreatedProperly(new MySQLSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyForOracle() {
        assertProjectionsContextCreatedProperly(new OracleSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyForPostgreSQL() {
        assertProjectionsContextCreatedProperly(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyForSQL92() {
        assertProjectionsContextCreatedProperly(new SQL92SelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyForSQLServer() {
        assertProjectionsContextCreatedProperly(new SQLServerSelectStatement());
    }
    
    private void assertProjectionsContextCreatedProperly(final SelectStatement selectStatement) {
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsSegment projectionsSegment = selectStatement.getProjections();
        ProjectionsContextEngine projectionsContextEngine = new ProjectionsContextEngine(DefaultDatabase.LOGIC_NAME, mockSchemas(), selectStatementContext.getDatabaseType());
        ProjectionsContext actual = projectionsContextEngine
                .createProjectionsContext(selectStatement.getFrom(), projectionsSegment, new GroupByContext(Collections.emptyList()), new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyWhenProjectionPresentForMySQL() {
        assertProjectionsContextCreatedProperlyWhenProjectionPresent(new MySQLSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyWhenProjectionPresentForOracle() {
        assertProjectionsContextCreatedProperlyWhenProjectionPresent(new OracleSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyWhenProjectionPresentForPostgreSQL() {
        assertProjectionsContextCreatedProperlyWhenProjectionPresent(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyWhenProjectionPresentForSQL92() {
        assertProjectionsContextCreatedProperlyWhenProjectionPresent(new SQL92SelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyWhenProjectionPresentForSQLServer() {
        assertProjectionsContextCreatedProperlyWhenProjectionPresent(new SQLServerSelectStatement());
    }
    
    private void assertProjectionsContextCreatedProperlyWhenProjectionPresent(final SelectStatement selectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(DefaultDatabase.LOGIC_NAME, mockSchemas(), selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(),
                projectionsSegment, new GroupByContext(Collections.emptyList()), new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWhenOrderByContextOrderItemsPresentForMySQL() {
        createProjectionsContextWhenOrderByContextOrderItemsPresent(new MySQLSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenOrderByContextOrderItemsPresentForOracle() {
        createProjectionsContextWhenOrderByContextOrderItemsPresent(new OracleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenOrderByContextOrderItemsPresentForPostgreSQL() {
        createProjectionsContextWhenOrderByContextOrderItemsPresent(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenOrderByContextOrderItemsPresentForSQL92() {
        createProjectionsContextWhenOrderByContextOrderItemsPresent(new SQL92SelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenOrderByContextOrderItemsPresentForSQLServer() {
        createProjectionsContextWhenOrderByContextOrderItemsPresent(new SQLServerSelectStatement());
    }
    
    private void createProjectionsContextWhenOrderByContextOrderItemsPresent(final SelectStatement selectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        OrderByItem orderByItem = new OrderByItem(new IndexOrderByItemSegment(0, 1, 0, OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(DefaultDatabase.LOGIC_NAME, mockSchemas(), selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(), projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWithoutIndexOrderByItemSegmentForMySQL() {
        assertCreateProjectionsContextWithoutIndexOrderByItemSegment(new MySQLSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithoutIndexOrderByItemSegmentForOracle() {
        assertCreateProjectionsContextWithoutIndexOrderByItemSegment(new OracleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithoutIndexOrderByItemSegmentForPostgreSQL() {
        assertCreateProjectionsContextWithoutIndexOrderByItemSegment(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithoutIndexOrderByItemSegmentForSQL92() {
        assertCreateProjectionsContextWithoutIndexOrderByItemSegment(new SQL92SelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithoutIndexOrderByItemSegmentForSQLServer() {
        assertCreateProjectionsContextWithoutIndexOrderByItemSegment(new SQLServerSelectStatement());
    }
    
    private void assertCreateProjectionsContextWithoutIndexOrderByItemSegment(final SelectStatement selectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        OrderByItem orderByItem = new OrderByItem(new ExpressionOrderByItemSegment(0, 1, "", OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(DefaultDatabase.LOGIC_NAME, mockSchemas(), selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(), projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    private SelectStatementContext createSelectStatementContext(final SelectStatement selectStatement) {
        ShardingSphereDatabase database = mockDatabase();
        when(database.getSchemas()).thenReturn(mockSchemas());
        Map<String, ShardingSphereDatabase> databases = Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
        return new SelectStatementContext(metaData, Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getRuleMetaData().findRules(TableContainedRule.class)).thenReturn(Collections.emptyList());
        return result;
    }
    
    private Map<String, ShardingSphereSchema> mockSchemas() {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(2, 1F);
        result.put(DefaultDatabase.LOGIC_NAME, schema);
        result.put("public", schema);
        return result;
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsentForMySQL() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(new MySQLSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsentForOracle() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(new OracleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsentForPostgreSQL() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsentForSQL92() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(new SQL92SelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsentForSQLServer() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(new SQLServerSelectStatement());
    }
    
    private void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(final SelectStatement selectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("name"))));
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        OrderByItem orderByItem = new OrderByItem(new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("name")), OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(DefaultDatabase.LOGIC_NAME, mockSchemas(), selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(), projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentForMySQL() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(new MySQLSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentForOracle() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(new OracleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentForPostgreSQL() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentForSQL92() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(new SQL92SelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentForSQLServer() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(new SQLServerSelectStatement());
    }
    
    private void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(final SelectStatement selectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("name"))));
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        OrderByItem orderByItem = new OrderByItem(new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("name")), OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(DefaultDatabase.LOGIC_NAME, mockSchemas(), selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(), projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresentForMySQL() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(new MySQLSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresentForOracle() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(new OracleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresentForPostgreSQL() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresentForSQL92() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(new SQL92SelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresentForSQLServer() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(new SQLServerSelectStatement());
    }
    
    private void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(final SelectStatement selectStatement) {
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(0, 10, new IdentifierValue("name")));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        tableSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue(DefaultDatabase.LOGIC_NAME)));
        selectStatement.setFrom(tableSegment);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 10, new IdentifierValue("name")));
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        table.setOwner(new OwnerSegment(0, 10, new IdentifierValue("name")));
        shorthandProjectionSegment.setOwner(owner);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("col"));
        columnSegment.setOwner(owner);
        ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(columnSegment);
        columnProjectionSegment.getColumn().setOwner(owner);
        projectionsSegment.getProjections().addAll(Arrays.asList(columnProjectionSegment, shorthandProjectionSegment));
        OrderByItem orderByItem = new OrderByItem(new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("name")), OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singleton(orderByItem), false);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(DefaultDatabase.LOGIC_NAME, mockSchemas(), selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(), projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWithTemporaryTableForMySQL() {
        assertCreateProjectionsContextWithTemporaryTable(new MySQLSelectStatement(), new MySQLSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithTemporaryTableForOracle() {
        assertCreateProjectionsContextWithTemporaryTable(new OracleSelectStatement(), new OracleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithTemporaryTableForPostgreSQL() {
        assertCreateProjectionsContextWithTemporaryTable(new PostgreSQLSelectStatement(), new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithTemporaryTableForSQL92() {
        assertCreateProjectionsContextWithTemporaryTable(new SQL92SelectStatement(), new SQL92SelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithTemporaryTableForSQLServer() {
        assertCreateProjectionsContextWithTemporaryTable(new SQLServerSelectStatement(), new SQLServerSelectStatement());
    }
    
    private void assertCreateProjectionsContextWithTemporaryTable(final SelectStatement selectStatement, final SelectStatement subquerySelectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        ShorthandProjectionSegment projectionSegment = new ShorthandProjectionSegment(0, 0);
        projectionSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("d")));
        projectionsSegment.getProjections().add(projectionSegment);
        selectStatement.setProjections(projectionsSegment);
        subquerySelectStatement.setProjections(new ProjectionsSegment(0, 0));
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(new SubquerySegment(0, 0, subquerySelectStatement));
        subqueryTableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("d")));
        selectStatement.setFrom(subqueryTableSegment);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("name"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("d")));
        OrderByItem groupByItem = new OrderByItem(new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, NullsOrderType.FIRST));
        GroupByContext groupByContext = new GroupByContext(Collections.singleton(groupByItem));
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(DefaultDatabase.LOGIC_NAME, mockSchemas(), selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(), projectionsSegment, groupByContext, new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCaseForMySQL() {
        assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(new MySQLSelectStatement(), new MySQLSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCaseForOracle() {
        assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(new OracleSelectStatement(), new OracleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCaseForPostgreSQL() {
        assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(new PostgreSQLSelectStatement(), new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCaseForSQL92() {
        assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(new SQL92SelectStatement(), new SQL92SelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCaseForSQLServer() {
        assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(new SQLServerSelectStatement(), new SQLServerSelectStatement());
    }
    
    private void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(final SelectStatement selectStatement, final SelectStatement subquerySelectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        ShorthandProjectionSegment projectionSegment = new ShorthandProjectionSegment(0, 0);
        projectionSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("table")));
        projectionsSegment.getProjections().add(projectionSegment);
        selectStatement.setProjections(projectionsSegment);
        subquerySelectStatement.setProjections(new ProjectionsSegment(0, 0));
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(new SubquerySegment(0, 0, subquerySelectStatement));
        subqueryTableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("TABLE")));
        selectStatement.setFrom(subqueryTableSegment);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("name"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("TAble")));
        OrderByItem groupByItem = new OrderByItem(new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, NullsOrderType.FIRST));
        GroupByContext groupByContext = new GroupByContext(Collections.singleton(groupByItem));
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(DefaultDatabase.LOGIC_NAME, mockSchemas(), selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(), projectionsSegment, groupByContext, new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWithOrderByExpressionForMySQL() {
        SelectStatement selectStatement = new MySQLSelectStatement();
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ShorthandProjectionSegment(0, 0));
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        OrderByItem orderByItem = new OrderByItem(new ExpressionOrderByItemSegment(0, 0, "id + 1", OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singleton(orderByItem), false);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(DefaultDatabase.LOGIC_NAME, mockSchemas(), selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(), projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertThat(actual.getProjections().size(), is(2));
    }
}
