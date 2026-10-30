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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.returning;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.ProjectionsSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProjectionsSegmentBinder.class)
class ReturningSegmentBinderTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getReturningAliases")
    void assertBindOutputAliases(final String name, final AliasSegment oldAlias, final AliasSegment newAlias, final String expectedOldName, final String expectedNewName) {
        ProjectionsSegment projections = new ProjectionsSegment(10, 20);
        ReturningSegment segment = new ReturningSegment(0, 20, projections);
        segment.setOldAlias(oldAlias);
        segment.setNewAlias(newAlias);
        DatabaseType databaseType = mock(DatabaseType.class);
        when(databaseType.getType()).thenReturn("PostgreSQL");
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        when(binderContext.getSqlStatement()).thenReturn(InsertStatement.builder().databaseType(databaseType).build());
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 8, new IdentifierValue("foo_table")));
        table.setAlias(new AliasSegment(10, 18, new IdentifierValue("foo_alias")));
        TableSegmentBinderContext targetContext = mock(TableSegmentBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableContexts = LinkedHashMultimap.create();
        tableContexts.put(CaseInsensitiveString.of("foo_alias"), targetContext);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerContexts = LinkedHashMultimap.create();
        outerContexts.put(CaseInsensitiveString.of("bar_outer"), mock(TableSegmentBinderContext.class));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> expectedContexts = LinkedHashMultimap.create(outerContexts);
        expectedContexts.put(CaseInsensitiveString.of(expectedOldName), targetContext);
        expectedContexts.put(CaseInsensitiveString.of(expectedNewName), targetContext);
        ProjectionsSegment expectedProjections = new ProjectionsSegment(10, 20);
        when(ProjectionsSegmentBinder.bind(projections, binderContext, table, tableContexts, expectedContexts)).thenReturn(expectedProjections);
        ReturningSegment actual = ReturningSegmentBinder.bind(segment, binderContext, table, tableContexts, outerContexts);
        assertThat(actual.getProjections(), sameInstance(expectedProjections));
        assertThat(outerContexts.size(), is(1));
        assertFalse(tableContexts.containsKey(CaseInsensitiveString.of(expectedOldName)));
    }
    
    private static Stream<Arguments> getReturningAliases() {
        AliasSegment oldAlias = new AliasSegment(0, 6, new IdentifierValue("foo_old"));
        AliasSegment newAlias = new AliasSegment(8, 14, new IdentifierValue("foo_new"));
        return Stream.of(Arguments.of("implicit output names", null, null, "old", "new"),
                Arguments.of("explicit output names", oldAlias, newAlias, "foo_old", "foo_new"),
                Arguments.of("one explicit output name", oldAlias, null, "foo_old", "new"));
    }
    
    @Test
    void assertBind() {
        ProjectionsSegment projections = new ProjectionsSegment(10, 20);
        ReturningSegment segment = new ReturningSegment(0, 20, projections);
        segment.setOldAlias(new AliasSegment(1, 3, new IdentifierValue("foo_old")));
        segment.setNewAlias(new AliasSegment(5, 7, new IdentifierValue("foo_new")));
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        TableSegment table = mock(TableSegment.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableContexts = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerContexts = LinkedHashMultimap.create();
        ProjectionsSegment expectedProjections = new ProjectionsSegment(10, 20);
        when(ProjectionsSegmentBinder.bind(projections, binderContext, table, tableContexts, outerContexts)).thenReturn(expectedProjections);
        ReturningSegment actual = ReturningSegmentBinder.bind(segment, binderContext, table, tableContexts, outerContexts);
        assertThat(actual.getStartIndex(), is(0));
        assertThat(actual.getStopIndex(), is(20));
        assertThat(actual.getProjections(), sameInstance(expectedProjections));
        assertThat(actual.getOldAlias(), sameInstance(segment.getOldAlias()));
        assertThat(actual.getNewAlias(), sameInstance(segment.getNewAlias()));
    }
}
