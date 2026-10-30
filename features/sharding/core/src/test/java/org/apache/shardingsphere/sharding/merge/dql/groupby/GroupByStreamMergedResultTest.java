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

package org.apache.shardingsphere.sharding.merge.dql.groupby;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.exception.data.NotImplementComparableValueException;
import org.apache.shardingsphere.sharding.merge.dql.ShardingDQLResultMerger;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroupByStreamMergedResultTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Test
    void assertNextForResultSetsAllEmpty() throws SQLException {
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        MergedResult actual = resultMerger.merge(Arrays.asList(mockQueryResult(), mockQueryResult(), mockQueryResult()),
                createSelectStatementContext(), mock(ShardingSphereDatabase.class), mock(ConnectionContext.class));
        assertFalse(actual.next());
    }
    
    private QueryResult mockQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(6);
        when(result.getMetaData().getColumnLabel(1)).thenReturn("COUNT(*)");
        when(result.getMetaData().getColumnLabel(2)).thenReturn("AVG(num)");
        when(result.getMetaData().getColumnLabel(3)).thenReturn("id");
        when(result.getMetaData().getColumnLabel(4)).thenReturn("date");
        when(result.getMetaData().getColumnLabel(5)).thenReturn("AVG_DERIVED_COUNT_0");
        when(result.getMetaData().getColumnLabel(6)).thenReturn("AVG_DERIVED_SUM_0");
        return result;
    }
    
    private SelectStatementContext createSelectStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        AggregationProjection countProjection = createAggregationProjection(AggregationType.COUNT, 1);
        AggregationProjection averageProjection = createAggregationProjection(AggregationType.AVG, 2);
        AggregationProjection derivedCountProjection = createAggregationProjection(AggregationType.COUNT, 5);
        AggregationProjection derivedSumProjection = createAggregationProjection(AggregationType.SUM, 6);
        when(averageProjection.getDerivedAggregationProjections()).thenReturn(Arrays.asList(derivedCountProjection, derivedSumProjection));
        when(result.getProjectionsContext().getAggregationProjections()).thenReturn(Arrays.asList(countProjection, averageProjection, derivedCountProjection, derivedSumProjection));
        OrderByItem orderByItem = new OrderByItem(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.ASC, NullsOrderType.FIRST));
        orderByItem.setIndex(3);
        when(result.getGroupByContext().getItems()).thenReturn(Collections.singletonList(orderByItem));
        when(result.getOrderByContext().getItems()).thenReturn(Collections.singletonList(orderByItem));
        when(result.isSameGroupByAndOrderByItems()).thenReturn(true);
        when(result.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        return result;
    }
    
    private AggregationProjection createAggregationProjection(final AggregationType aggregationType, final int index) {
        AggregationProjection result = mock(AggregationProjection.class);
        when(result.getType()).thenReturn(aggregationType);
        when(result.getIndex()).thenReturn(index);
        return result;
    }
    
    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Test
    void assertNextForSomeResultSetsEmpty() throws SQLException {
        QueryResult queryResult1 = mockQueryResult();
        when(queryResult1.next()).thenReturn(true, false);
        when(queryResult1.getValue(1, Object.class)).thenReturn(20);
        when(queryResult1.getValue(2, Object.class)).thenReturn(0);
        when(queryResult1.getValue(3, Object.class)).thenReturn(2);
        when(queryResult1.getValue(4, Object.class)).thenReturn(new Date(0L));
        when(queryResult1.getValue(5, Object.class)).thenReturn(2);
        when(queryResult1.getValue(6, Object.class)).thenReturn(20);
        QueryResult queryResult2 = mockQueryResult();
        QueryResult queryResult3 = mockQueryResult();
        when(queryResult3.next()).thenReturn(true, true, false);
        when(queryResult3.getValue(1, Object.class)).thenReturn(20, 30);
        when(queryResult3.getValue(2, Object.class)).thenReturn(0);
        when(queryResult3.getValue(3, Object.class)).thenReturn(2, 2, 3);
        when(queryResult3.getValue(4, Object.class)).thenReturn(new Date(0L));
        when(queryResult3.getValue(5, Object.class)).thenReturn(2, 2, 3);
        when(queryResult3.getValue(6, Object.class)).thenReturn(20, 20, 30);
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        MergedResult actual =
                resultMerger.merge(Arrays.asList(queryResult1, queryResult2, queryResult3), createSelectStatementContext(), mock(ShardingSphereDatabase.class), mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(40)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(2));
        assertThat(actual.getCalendarValue(4, Date.class, Calendar.getInstance()), is(new Date(0L)));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(4)));
        assertThat(actual.getValue(6, Object.class), is(new BigDecimal(40)));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(30)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(3));
        assertThat(actual.getCalendarValue(4, Date.class, Calendar.getInstance()), is(new Date(0L)));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(3)));
        assertThat(actual.getValue(6, Object.class), is(new BigDecimal(30)));
        assertFalse(actual.next());
    }
    
    @Test
    void assertNextForMix() throws SQLException {
        QueryResult queryResult1 = mockQueryResult();
        when(queryResult1.next()).thenReturn(true, false);
        when(queryResult1.getValue(1, Object.class)).thenReturn(20);
        when(queryResult1.getValue(2, Object.class)).thenReturn(0);
        when(queryResult1.getValue(3, Object.class)).thenReturn(2);
        when(queryResult1.getValue(5, Object.class)).thenReturn(2);
        when(queryResult1.getValue(6, Object.class)).thenReturn(20);
        QueryResult queryResult2 = mockQueryResult();
        when(queryResult2.next()).thenReturn(true, true, true, false);
        when(queryResult2.getValue(1, Object.class)).thenReturn(20, 30, 40);
        when(queryResult2.getValue(2, Object.class)).thenReturn(0);
        when(queryResult2.getValue(3, Object.class)).thenReturn(2, 2, 3, 3, 3, 4);
        when(queryResult2.getValue(5, Object.class)).thenReturn(2, 2, 3, 3, 3, 4);
        when(queryResult2.getValue(6, Object.class)).thenReturn(20, 20, 30, 30, 30, 40);
        QueryResult queryResult3 = mockQueryResult();
        when(queryResult3.next()).thenReturn(true, true, false);
        when(queryResult3.getValue(1, Object.class)).thenReturn(10, 30);
        when(queryResult3.getValue(2, Object.class)).thenReturn(10);
        when(queryResult3.getValue(3, Object.class)).thenReturn(1, 1, 1, 1, 3);
        when(queryResult3.getValue(5, Object.class)).thenReturn(1, 1, 3);
        when(queryResult3.getValue(6, Object.class)).thenReturn(10, 10, 30);
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        MergedResult actual =
                resultMerger.merge(Arrays.asList(queryResult1, queryResult2, queryResult3), createSelectStatementContext(), mock(ShardingSphereDatabase.class), mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(10)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(1));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(1)));
        assertThat(actual.getValue(6, Object.class), is(new BigDecimal(10)));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(40)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(2));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(4)));
        assertThat(actual.getValue(6, Object.class), is(new BigDecimal(40)));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(60)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(3));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(6)));
        assertThat(actual.getValue(6, Object.class), is(new BigDecimal(60)));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(40)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(4));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(4)));
        assertThat(actual.getValue(6, Object.class), is(new BigDecimal(40)));
        assertFalse(actual.next());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("groupConcatArguments")
    void assertNextWithDistinctGroupConcat(final String name, final int columnType, final Comparable<?> value, final String expected) throws SQLException {
        QueryResult queryResult = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(queryResult.next()).thenReturn(true, false);
        when(queryResult.getMetaData().getColumnCount()).thenReturn(1);
        when(queryResult.getValue(1, Object.class)).thenReturn(value);
        if (value instanceof LocalTime) {
            when(queryResult.getMetaData().getColumnType(1)).thenReturn(columnType);
        }
        AggregationProjection projection = mock(AggregationDistinctProjection.class);
        when(projection.getIndex()).thenReturn(1);
        when(projection.getType()).thenReturn(AggregationType.GROUP_CONCAT);
        GroupByStreamMergedResult actual = new GroupByStreamMergedResult(Collections.singletonMap("value", 1), Collections.singletonList(queryResult), createAggregationContext(projection), mock());
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(expected));
        assertFalse(actual.next());
    }
    
    private SelectStatementContext createAggregationContext(final AggregationProjection projection) {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getProjectionsContext().getAggregationProjections()).thenReturn(Collections.singletonList(projection));
        return result;
    }
    
    private static Stream<Arguments> groupConcatArguments() {
        return Stream.of(
                Arguments.of("whole-minute", Types.TIME, LocalTime.of(12, 34), "12:34:00"),
                Arguments.of("microseconds", Types.TIME, LocalTime.of(12, 34, 56, 123456000), "12:34:56.123456"),
                Arguments.of("midnight-microsecond", Types.TIME, LocalTime.of(0, 0, 0, 1000), "00:00:00.000001"),
                Arguments.of("generic-object", Types.JAVA_OBJECT, LocalTime.of(12, 34), "12:34"),
                Arguments.of("non-time", Types.TIMESTAMP, LocalTime.of(12, 34), "12:34"),
                Arguments.of("legacy-time", Types.TIME, Time.valueOf("12:34:00"), "12:34:00"),
                Arguments.of("string", Types.VARCHAR, "foo", "foo"));
    }
    
    @ParameterizedTest
    @MethodSource("otherAggregationArguments")
    void assertNextWithOtherDistinctAggregation(final AggregationType aggregationType, final Comparable<?> expected) throws SQLException {
        QueryResult queryResult = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(queryResult.next()).thenReturn(true, false);
        when(queryResult.getMetaData().getColumnCount()).thenReturn(1);
        when(queryResult.getValue(1, Object.class)).thenReturn(LocalTime.of(12, 34));
        AggregationProjection projection = mock(AggregationDistinctProjection.class);
        when(projection.getIndex()).thenReturn(1);
        when(projection.getType()).thenReturn(aggregationType);
        GroupByStreamMergedResult actual = new GroupByStreamMergedResult(Collections.singletonMap("value", 1), Collections.singletonList(queryResult), createAggregationContext(projection), mock());
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(expected));
        assertFalse(actual.next());
    }
    
    private static Stream<Arguments> otherAggregationArguments() {
        return Stream.of(Arguments.of(AggregationType.MIN, LocalTime.of(12, 34)), Arguments.of(AggregationType.MAX, LocalTime.of(12, 34)), Arguments.of(AggregationType.COUNT, 1));
    }
    
    @Test
    void assertNextWithNonDistinctGroupConcat() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(queryResult.next()).thenReturn(true, false);
        when(queryResult.getMetaData().getColumnCount()).thenReturn(1);
        when(queryResult.getValue(1, Object.class)).thenReturn(LocalTime.of(12, 34));
        GroupByStreamMergedResult actual = new GroupByStreamMergedResult(Collections.singletonMap("value", 1), Collections.singletonList(queryResult),
                createAggregationContext(createAggregationProjection(AggregationType.GROUP_CONCAT, 1)), mock());
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is("12:34"));
        assertFalse(actual.next());
    }
    
    @Test
    void assertNextWithNullGroupConcat() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(queryResult.next()).thenReturn(true, false);
        when(queryResult.getMetaData().getColumnCount()).thenReturn(1);
        AggregationProjection projection = mock(AggregationDistinctProjection.class);
        when(projection.getIndex()).thenReturn(1);
        when(projection.getType()).thenReturn(AggregationType.GROUP_CONCAT);
        GroupByStreamMergedResult actual = new GroupByStreamMergedResult(Collections.singletonMap("value", 1), Collections.singletonList(queryResult), createAggregationContext(projection), mock());
        assertTrue(actual.next());
        assertNull(actual.getValue(1, Object.class));
        assertTrue(actual.wasNull());
        assertFalse(actual.next());
    }
    
    @Test
    void assertNextWithNonComparableValue() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(queryResult.next()).thenReturn(true, false);
        when(queryResult.getValue(1, Object.class)).thenReturn(new Object());
        GroupByStreamMergedResult actual = new GroupByStreamMergedResult(Collections.singletonMap("value", 1), Collections.singletonList(queryResult),
                createAggregationContext(createAggregationProjection(AggregationType.MAX, 1)), mock());
        assertThrows(NotImplementComparableValueException.class, actual::next);
    }
}
