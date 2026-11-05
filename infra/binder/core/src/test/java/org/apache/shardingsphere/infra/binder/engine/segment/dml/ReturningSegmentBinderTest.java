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

package org.apache.shardingsphere.infra.binder.engine.segment.dml;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.ProjectionsSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProjectionsSegmentBinder.class)
class ReturningSegmentBinderTest {
    
    @Captor
    private ArgumentCaptor<Multimap<CaseInsensitiveString, TableSegmentBinderContext>> tableContextsCaptor;
    
    @Test
    void assertBindPreservesMetadata() {
        ProjectionsSegment projections = new ProjectionsSegment(10, 20);
        ProjectionsSegment bound = new ProjectionsSegment(10, 20);
        ReturningSegment segment = new ReturningSegment(0, 30, projections);
        AliasSegment oldAlias = new AliasSegment(1, 4, new IdentifierValue("old_row"));
        AliasSegment newAlias = new AliasSegment(5, 8, new IdentifierValue("new_row"));
        segment.setOldAlias(oldAlias);
        segment.setNewAlias(newAlias);
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class);
        SimpleTableSegment table = mock(SimpleTableSegment.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tables = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outer = LinkedHashMultimap.create();
        when(ProjectionsSegmentBinder.bind(projections, context, table, tables, outer)).thenReturn(bound);
        ReturningSegment actual = ReturningSegmentBinder.bind(segment, context, table, tables, outer);
        assertThat(actual.getProjections(), sameInstance(bound));
        assertThat(actual.getStartIndex(), is(0));
        assertThat(actual.getStopIndex(), is(30));
        assertThat(actual.getOldAlias(), sameInstance(oldAlias));
        assertThat(actual.getNewAlias(), sameInstance(newAlias));
        assertThat(segment.getProjections(), sameInstance(projections));
    }
    
    @ParameterizedTest(name = "owner={0}, existing={1}, rowAlias={2}")
    @CsvSource({"OLD,false,true", "NEW,false,true", "foo_old,false,true", "OLD,true,true", "foo_other,false,false"})
    void assertBindRowAlias(final String owner, final boolean existing, final boolean rowAlias) {
        ProjectionsSegment projections = new ProjectionsSegment(10, 20);
        ShorthandProjectionSegment shorthand = new ShorthandProjectionSegment(10, 20);
        shorthand.setOwner(new OwnerSegment(10, 16, new IdentifierValue(owner)));
        projections.getProjections().add(shorthand);
        ReturningSegment segment = new ReturningSegment(0, 30, projections);
        if ("foo_old".equals(owner)) {
            segment.setOldAlias(new AliasSegment(1, 7, new IdentifierValue(owner)));
        }
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 8, new IdentifierValue("foo_table")));
        table.setAlias(new AliasSegment(10, 12, new IdentifierValue("foo_alias")));
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class);
        TableSegmentBinderContext targetContext = mock(TableSegmentBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tables = LinkedHashMultimap.create();
        tables.put(CaseInsensitiveString.of("foo_alias"), targetContext);
        if (existing) {
            tables.put(CaseInsensitiveString.of(owner), targetContext);
        }
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outer = LinkedHashMultimap.create();
        ProjectionsSegment expectedProjections = new ProjectionsSegment(10, 20);
        when(ProjectionsSegmentBinder.bind(eq(projections), eq(context), eq(table), tableContextsCaptor.capture(), eq(outer))).thenReturn(expectedProjections);
        ReturningSegment actual = ReturningSegmentBinder.bind(segment, context, table, tables, outer);
        assertThat(actual.getProjections(), sameInstance(expectedProjections));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> actualContexts = tableContextsCaptor.getValue();
        if (rowAlias && !existing) {
            assertThat(actualContexts, not(sameInstance(tables)));
            assertTrue(actualContexts.containsEntry(CaseInsensitiveString.of(owner), targetContext));
        } else {
            assertThat(actualContexts, sameInstance(tables));
        }
        assertThat(tables.size(), is(existing ? 2 : 1));
    }
    
    @Test
    void assertBindDeleteRelationScope() {
        ProjectionsSegment projections = new ProjectionsSegment(10, 20);
        ProjectionsSegment bound = new ProjectionsSegment(10, 20);
        ReturningSegment segment = new ReturningSegment(0, 30, projections);
        SimpleTableSegment target = mock(SimpleTableSegment.class);
        DeleteMultiTableSegment table = new DeleteMultiTableSegment();
        table.getActualDeleteTables().add(target);
        JoinTableSegment relation = mock(JoinTableSegment.class);
        table.setRelationTable(relation);
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tables = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outer = LinkedHashMultimap.create();
        when(ProjectionsSegmentBinder.bind(projections, context, relation, tables, outer)).thenReturn(bound);
        ReturningSegment actual = ReturningSegmentBinder.bind(segment, context, table, tables, outer);
        assertThat(actual.getProjections(), sameInstance(bound));
    }
}
