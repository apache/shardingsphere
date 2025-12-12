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

package org.apache.shardingsphere.infra.binder.context.segment.select.subquery.engine;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.subquery.SubqueryTableContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubqueryTableContextEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertCreateSubqueryTableContextsWithSimpleTableSegment() {
        SelectStatementContext subqueryContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(subqueryContext.getSqlStatement().getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")))));
        ColumnProjection columnProjection = new ColumnProjection("o", "foo_col", "a", databaseType);
        List<Projection> projections = Arrays.asList(mock(Projection.class), columnProjection);
        when(subqueryContext.getProjectionsContext().getExpandProjections()).thenReturn(projections);
        Map<String, SubqueryTableContext> actual = new SubqueryTableContextEngine().createSubqueryTableContexts(subqueryContext, "alias");
        assertThat(actual.size(), is(1));
        assertThat(actual.get("foo_tbl").getTableName(), is("foo_tbl"));
        assertThat(actual.get("foo_tbl").getAliasName(), is("alias"));
        assertThat(actual.get("foo_tbl").getColumnNames(), is(Collections.singletonList("foo_col")));
    }
    
    @Test
    void assertCreateSubqueryTableContextsWithJoinTableSegmentAndPresentColumnOwner() {
        SelectStatementContext subqueryContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(subqueryContext.getTablesContext().getSimpleTables()).thenReturn(Collections.singleton(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")))));
        when(subqueryContext.getSqlStatement().getFrom()).thenReturn(Optional.of(mock(JoinTableSegment.class)));
        ColumnProjection columnProjection = new ColumnProjection("foo_tbl", "foo_col", "a", databaseType);
        List<Projection> projections = Arrays.asList(mock(Projection.class), columnProjection);
        when(subqueryContext.getProjectionsContext().getExpandProjections()).thenReturn(projections);
        Map<String, SubqueryTableContext> actual = new SubqueryTableContextEngine().createSubqueryTableContexts(subqueryContext, "alias");
        assertThat(actual.size(), is(1));
        assertThat(actual.get("foo_tbl").getTableName(), is("foo_tbl"));
        assertThat(actual.get("foo_tbl").getAliasName(), is("alias"));
        assertThat(actual.get("foo_tbl").getColumnNames(), is(Collections.singletonList("foo_col")));
    }
    
    @Test
    void assertCreateSubqueryTableContextsWithJoinTableSegmentAndMisMatchedColumnOwner() {
        SelectStatementContext subqueryContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(subqueryContext.getTablesContext().getSimpleTables()).thenReturn(Collections.singleton(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")))));
        when(subqueryContext.getSqlStatement().getFrom()).thenReturn(Optional.of(mock(JoinTableSegment.class)));
        ColumnProjection columnProjection = new ColumnProjection("o", "foo_col", "a", databaseType);
        List<Projection> projections = Arrays.asList(mock(Projection.class), columnProjection);
        when(subqueryContext.getProjectionsContext().getExpandProjections()).thenReturn(projections);
        Map<String, SubqueryTableContext> actual = new SubqueryTableContextEngine().createSubqueryTableContexts(subqueryContext, "alias");
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertCreateSubqueryTableContextsWithJoinTableSegmentAndAbsentColumnOwner() {
        SelectStatementContext subqueryContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(subqueryContext.getSqlStatement().getFrom()).thenReturn(Optional.of(mock(JoinTableSegment.class)));
        ColumnProjection columnProjection = new ColumnProjection(null, "foo_col", "a", databaseType);
        List<Projection> projections = Arrays.asList(mock(Projection.class), columnProjection);
        when(subqueryContext.getProjectionsContext().getExpandProjections()).thenReturn(projections);
        Map<String, SubqueryTableContext> actual = new SubqueryTableContextEngine().createSubqueryTableContexts(subqueryContext, "alias");
        assertTrue(actual.isEmpty());
    }
}
