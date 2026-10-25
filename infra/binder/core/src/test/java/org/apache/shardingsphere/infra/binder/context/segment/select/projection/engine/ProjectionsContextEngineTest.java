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

package org.apache.shardingsphere.infra.binder.context.segment.select.projection.engine;

import com.google.common.collect.Iterables;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectionsContextEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @ParameterizedTest(name = "{0}")
    @CsvSource({"explicit alias first,AGGREGATION_DISTINCT_DERIVED_0,true", "explicit alias last,AGGREGATION_DISTINCT_DERIVED_0,false",
            "folded explicit alias first,aggregation_distinct_derived_0,true", "folded explicit alias last,aggregation_distinct_derived_0,false"})
    void assertCreateProjectionsContextWithAggregationAliasCollision(final String name, final String alias, final boolean explicitFirst) {
        AggregationDistinctProjectionSegment explicit = new AggregationDistinctProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(DISTINCT user_id)", "user_id");
        explicit.setAlias(new AliasSegment(0, 0, new IdentifierValue(alias)));
        AggregationDistinctProjectionSegment generated = new AggregationDistinctProjectionSegment(0, 0, AggregationType.SUM, "SUM(DISTINCT order_id)", "order_id");
        Collection<ProjectionSegment> segments = explicitFirst ? Arrays.asList(explicit, generated) : Arrays.asList(generated, explicit);
        Iterator<Projection> actual =
                new ProjectionsContextEngine(databaseType).createProjectionsContext(createProjectionsSegment(segments), new GroupByContext(Collections.emptyList()),
                        new OrderByContext(Collections.emptyList(), false)).getProjections().iterator();
        assertThat(actual.next().getAlias().get().getValue(), is(explicitFirst ? alias : "AGGREGATION_DISTINCT_DERIVED_1"));
        assertThat(actual.next().getAlias().get().getValue(), is(explicitFirst ? "AGGREGATION_DISTINCT_DERIVED_1" : alias));
    }
    
    private ProjectionsSegment createProjectionsSegment(final Collection<ProjectionSegment> projections) {
        ProjectionsSegment result = new ProjectionsSegment(0, 0);
        result.getProjections().addAll(projections);
        return result;
    }
    
    @Test
    void assertCreateProjectionsContextWithColumnLabelCollision() {
        ColumnProjectionSegment column = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("AGGREGATION_DISTINCT_DERIVED_0")));
        AggregationDistinctProjectionSegment count = new AggregationDistinctProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(DISTINCT user_id)", "user_id");
        Collection<Projection> actual =
                new ProjectionsContextEngine(databaseType).createProjectionsContext(createProjectionsSegment(Arrays.asList(count, column)), new GroupByContext(Collections.emptyList()),
                        new OrderByContext(Collections.emptyList(), false)).getProjections();
        assertThat(actual.iterator().next().getAlias().get().getValue(), is("AGGREGATION_DISTINCT_DERIVED_1"));
    }
    
    @Test
    void assertCreateProjectionsContextWithShorthandColumnLabelCollision() {
        ColumnProjectionSegment column = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("AGGREGATION_DISTINCT_DERIVED_0")));
        ShorthandProjectionSegment wildcard = new ShorthandProjectionSegment(0, 0);
        wildcard.getActualProjectionSegments().add(column);
        AggregationDistinctProjectionSegment count = new AggregationDistinctProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(DISTINCT user_id)", "user_id");
        Collection<ProjectionSegment> segments = Arrays.asList(count, wildcard);
        Collection<Projection> actual =
                new ProjectionsContextEngine(databaseType).createProjectionsContext(createProjectionsSegment(segments), new GroupByContext(Collections.emptyList()),
                        new OrderByContext(Collections.emptyList(), false)).getProjections();
        assertThat(actual.iterator().next().getAlias().get().getValue(), is("AGGREGATION_DISTINCT_DERIVED_1"));
    }
    
    @ParameterizedTest(name = "{0}")
    @CsvSource({"distinct average count collision,AVG_DERIVED_COUNT_0,true", "distinct average sum collision,avg_derived_sum_0,true",
            "average count collision,AVG_DERIVED_COUNT_0,false", "average sum collision,avg_derived_sum_0,false"})
    void assertCreateProjectionsContextWithAverageAliasCollision(final String name, final String alias, final boolean distinct) {
        ColumnProjectionSegment column = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id")));
        column.setAlias(new AliasSegment(0, 0, new IdentifierValue(alias)));
        AggregationProjectionSegment average = distinct
                ? new AggregationDistinctProjectionSegment(0, 0, AggregationType.AVG, "AVG(DISTINCT user_id)", "user_id")
                : new AggregationProjectionSegment(0, 0, AggregationType.AVG, "AVG(user_id)");
        Collection<Projection> actualProjections =
                new ProjectionsContextEngine(databaseType).createProjectionsContext(createProjectionsSegment(Arrays.asList(average, column)), new GroupByContext(Collections.emptyList()),
                        new OrderByContext(Collections.emptyList(), false)).getProjections();
        AggregationProjection actual = (AggregationProjection) actualProjections.iterator().next();
        assertThat(actual.getDerivedAggregationProjections().get(0).getAlias().get().getValue(), is("AVG_DERIVED_COUNT_1"));
        assertThat(actual.getDerivedAggregationProjections().get(1).getAlias().get().getValue(), is("AVG_DERIVED_SUM_1"));
    }
    
    @Test
    void assertCreateProjectionsContextWithRepeatedAggregations() {
        AggregationDistinctProjectionSegment first = new AggregationDistinctProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(DISTINCT user_id)", "user_id");
        AggregationDistinctProjectionSegment second = new AggregationDistinctProjectionSegment(1, 1, AggregationType.COUNT, "COUNT(DISTINCT user_id)", "user_id");
        Iterator<Projection> actual =
                new ProjectionsContextEngine(databaseType).createProjectionsContext(createProjectionsSegment(Arrays.asList(first, second)), new GroupByContext(Collections.emptyList()),
                        new OrderByContext(Collections.emptyList(), false)).getProjections().iterator();
        assertThat(actual.next().getAlias().get().getValue(), is("AGGREGATION_DISTINCT_DERIVED_0"));
        assertThat(actual.next().getAlias().get().getValue(), is("AGGREGATION_DISTINCT_DERIVED_1"));
    }
    
    @Test
    void assertProjectionsContextCreatedProperly() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        ProjectionsContextEngine projectionsContextEngine = new ProjectionsContextEngine(databaseType);
        ProjectionsContext actual = projectionsContextEngine.createProjectionsContext(
                projectionsSegment, new GroupByContext(Collections.emptyList()), new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyWhenProjectionPresent() {
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        owner.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        shorthandProjectionSegment.setOwner(owner);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(databaseType);
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWhenOrderByContextOrderItemsPresent() {
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        owner.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        shorthandProjectionSegment.setOwner(owner);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        OrderByItem orderByItem = new OrderByItem(new IndexOrderByItemSegment(0, 1, 0, OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(databaseType);
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWithoutIndexOrderByItemSegment() {
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        owner.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        shorthandProjectionSegment.setOwner(owner);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        OrderByItem orderByItem = new OrderByItem(new ExpressionOrderByItemSegment(0, 1, "", OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(databaseType);
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent() {
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        owner.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        shorthandProjectionSegment.setOwner(owner);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        OrderByItem orderByItem = new OrderByItem(new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("name")), OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(databaseType);
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent() {
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        owner.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("col"));
        columnSegment.setOwner(owner);
        ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(columnSegment);
        columnProjectionSegment.getColumn().setOwner(owner);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        shorthandProjectionSegment.setOwner(owner);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().addAll(Arrays.asList(columnProjectionSegment, shorthandProjectionSegment));
        OrderByItem orderByItem = new OrderByItem(new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("name")), OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singleton(orderByItem), false);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(databaseType);
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWithTemporaryTable() {
        ShorthandProjectionSegment projectionSegment = new ShorthandProjectionSegment(0, 0);
        projectionSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("d")));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(projectionSegment);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("name"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("d")));
        OrderByItem groupByItem = new OrderByItem(new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, NullsOrderType.FIRST));
        GroupByContext groupByContext = new GroupByContext(Collections.singleton(groupByItem));
        ProjectionsContextEngine engine = new ProjectionsContextEngine(databaseType);
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, groupByContext, new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase() {
        ShorthandProjectionSegment projectionSegment = new ShorthandProjectionSegment(0, 0);
        projectionSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("table")));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(projectionSegment);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("name"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("TAble")));
        OrderByItem groupByItem = new OrderByItem(new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, NullsOrderType.FIRST));
        GroupByContext groupByContext = new GroupByContext(Collections.singleton(groupByItem));
        ProjectionsContextEngine engine = new ProjectionsContextEngine(databaseType);
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, groupByContext, new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWithOrderByExpression() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ShorthandProjectionSegment(0, 0));
        OrderByItem orderByItem = new OrderByItem(new ExpressionOrderByItemSegment(0, 0, "id + 1", OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singleton(orderByItem), false);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(databaseType);
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertThat(actual.getProjections().size(), is(2));
    }
    
    @Test
    void assertCreateProjectionsContextWithNullOrderByText() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ShorthandProjectionSegment(0, 0));
        OrderByItem orderByItem = new OrderByItem(new ExpressionOrderByItemSegment(0, 0, null, OrderDirection.ASC, NullsOrderType.FIRST));
        ProjectionsContextEngine engine = new ProjectionsContextEngine(databaseType);
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), new OrderByContext(Collections.singleton(orderByItem), false));
        assertThat(Iterables.getOnlyElement(actual.getProjections()).getExpression(), is("*"));
    }
    
    @Test
    void assertCreateProjectionsContextWithNullGroupByText() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ShorthandProjectionSegment(0, 0));
        OrderByItem groupByItem = new OrderByItem(new ExpressionOrderByItemSegment(0, 0, null, OrderDirection.ASC, NullsOrderType.FIRST));
        ProjectionsContextEngine engine = new ProjectionsContextEngine(databaseType);
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.singleton(groupByItem)), new OrderByContext(Collections.emptyList(), false));
        assertThat(Iterables.getOnlyElement(actual.getProjections()).getExpression(), is("*"));
    }
    
    @Test
    void assertCreateProjectionsContextWithNullOrderByTextAndEmptyProjections() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        OrderByItem orderByItem = new OrderByItem(new ExpressionOrderByItemSegment(0, 0, null, OrderDirection.ASC, NullsOrderType.FIRST));
        ProjectionsContextEngine engine = new ProjectionsContextEngine(databaseType);
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), new OrderByContext(Collections.singleton(orderByItem), false));
        assertTrue(actual.getProjections().isEmpty());
    }
    
    @Test
    void assertCreateProjectionsContextWithMatchingColumnName() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("foo_column"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_table")));
        projectionsSegment.getProjections().add(new ColumnProjectionSegment(columnSegment));
        ColumnSegment orderColumn = new ColumnSegment(0, 0, new IdentifierValue("`FOO_COLUMN`"));
        OrderByItem orderByItem = new OrderByItem(new ColumnOrderByItemSegment(orderColumn, OrderDirection.ASC, NullsOrderType.FIRST));
        ProjectionsContextEngine engine = new ProjectionsContextEngine(databaseType);
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), new OrderByContext(Collections.singleton(orderByItem), false));
        assertThat(actual.getProjections().size(), is(1));
    }
    
    @Test
    void assertCreateProjectionsContextWithMatchingQualifiedColumnName() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("foo_column"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_table")));
        projectionsSegment.getProjections().add(new ColumnProjectionSegment(columnSegment));
        ColumnSegment orderColumn = new ColumnSegment(0, 0, new IdentifierValue("\"FOO_COLUMN\""));
        orderColumn.setOwner(new OwnerSegment(0, 0, new IdentifierValue("\"FOO_TABLE\"")));
        OrderByItem orderByItem = new OrderByItem(new ColumnOrderByItemSegment(orderColumn, OrderDirection.ASC, NullsOrderType.FIRST));
        ProjectionsContextEngine engine = new ProjectionsContextEngine(databaseType);
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), new OrderByContext(Collections.singleton(orderByItem), false));
        assertThat(actual.getProjections().size(), is(1));
    }
    
    @Test
    void assertCreateProjectionsContextWithMatchingAlias() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        ExpressionProjectionSegment projectionSegment = new ExpressionProjectionSegment(0, 0, "foo_column + 1");
        projectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("`foo_alias`")));
        projectionsSegment.getProjections().add(projectionSegment);
        OrderByItem orderByItem = new OrderByItem(new ExpressionOrderByItemSegment(0, 0, "`FOO_ALIAS`", OrderDirection.ASC, NullsOrderType.FIRST));
        ProjectionsContextEngine engine = new ProjectionsContextEngine(databaseType);
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), new OrderByContext(Collections.singleton(orderByItem), false));
        assertThat(actual.getProjections().size(), is(1));
    }
    
    @Test
    void assertCreateProjectionsContextWithMatchingExpression() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ExpressionProjectionSegment(0, 0, "`foo_column` + 1"));
        OrderByItem orderByItem = new OrderByItem(new ExpressionOrderByItemSegment(0, 0, "`FOO_COLUMN` + 1", OrderDirection.ASC, NullsOrderType.FIRST));
        ProjectionsContextEngine engine = new ProjectionsContextEngine(databaseType);
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), new OrderByContext(Collections.singleton(orderByItem), false));
        assertThat(actual.getProjections().size(), is(1));
    }
}
