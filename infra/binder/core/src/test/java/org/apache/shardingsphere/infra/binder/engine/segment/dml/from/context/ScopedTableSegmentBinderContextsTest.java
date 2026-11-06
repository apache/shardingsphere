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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScopedTableSegmentBinderContextsTest {
    
    @Test
    void assertGetNearestScopeTableBinderContextsWithShadowingCurrentScope() {
        TableSegmentBinderContext outerTableBinderContext = mock(TableSegmentBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        outerTableBinderContexts.put(CaseInsensitiveString.of("foo_alias"), outerTableBinderContext);
        TableSegmentBinderContext fooTableBinderContext = mock(TableSegmentBinderContext.class);
        TableSegmentBinderContext barTableBinderContext = mock(TableSegmentBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> currentTableBinderContexts = LinkedHashMultimap.create();
        currentTableBinderContexts.put(CaseInsensitiveString.of("foo_alias"), fooTableBinderContext);
        currentTableBinderContexts.put(CaseInsensitiveString.of("foo_alias"), barTableBinderContext);
        ScopedTableSegmentBinderContexts actual = new ScopedTableSegmentBinderContexts(outerTableBinderContexts, currentTableBinderContexts);
        assertThat(actual.getNearestScopeTableBinderContexts(new OwnerSegment(0, 0, new IdentifierValue("foo_alias"))), contains(fooTableBinderContext, barTableBinderContext));
        assertThat(actual.get(CaseInsensitiveString.of("foo_alias")), contains(outerTableBinderContext, fooTableBinderContext, barTableBinderContext));
    }
    
    @Test
    void assertGetNearestScopeTableBinderContextsWithOuterScopeOnly() {
        TableSegmentBinderContext outerTableBinderContext = mock(TableSegmentBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        outerTableBinderContexts.put(CaseInsensitiveString.of("foo_alias"), outerTableBinderContext);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> currentTableBinderContexts = LinkedHashMultimap.create();
        currentTableBinderContexts.put(CaseInsensitiveString.of("bar_alias"), mock(TableSegmentBinderContext.class));
        ScopedTableSegmentBinderContexts actual = new ScopedTableSegmentBinderContexts(outerTableBinderContexts, currentTableBinderContexts);
        assertThat(actual.getNearestScopeTableBinderContexts(new OwnerSegment(0, 0, new IdentifierValue("foo_alias"))), contains(outerTableBinderContext));
    }
    
    @Test
    void assertGetNearestScopeTableBinderContextsWithScopedOuterScope() {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> fartherTableBinderContexts = LinkedHashMultimap.create();
        fartherTableBinderContexts.put(CaseInsensitiveString.of("foo_alias"), mock(TableSegmentBinderContext.class));
        TableSegmentBinderContext nearerTableBinderContext = mock(TableSegmentBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> nearerTableBinderContexts = LinkedHashMultimap.create();
        nearerTableBinderContexts.put(CaseInsensitiveString.of("foo_alias"), nearerTableBinderContext);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> currentTableBinderContexts = LinkedHashMultimap.create();
        currentTableBinderContexts.put(CaseInsensitiveString.of("bar_alias"), mock(TableSegmentBinderContext.class));
        ScopedTableSegmentBinderContexts actual = new ScopedTableSegmentBinderContexts(new ScopedTableSegmentBinderContexts(fartherTableBinderContexts, nearerTableBinderContexts),
                currentTableBinderContexts);
        assertThat(actual.getNearestScopeTableBinderContexts(new OwnerSegment(0, 0, new IdentifierValue("foo_alias"))), contains(nearerTableBinderContext));
    }
    
    @Test
    void assertGetNearestScopeTableBinderContextsWithDatabaseQualifiedOwner() {
        TableSegmentBinderContext fooTableBinderContext = mockTableBinderContext("foo_db", "foo_db");
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> fartherTableBinderContexts = LinkedHashMultimap.create();
        fartherTableBinderContexts.put(CaseInsensitiveString.of("t_order"), fooTableBinderContext);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> nearerTableBinderContexts = LinkedHashMultimap.create();
        nearerTableBinderContexts.put(CaseInsensitiveString.of("t_order"), mockTableBinderContext("bar_db", "bar_db"));
        ScopedTableSegmentBinderContexts actual = new ScopedTableSegmentBinderContexts(fartherTableBinderContexts, nearerTableBinderContexts);
        assertThat(actual.getNearestScopeTableBinderContexts(createQualifiedOwnerSegment("foo_db", "t_order")), contains(fooTableBinderContext));
    }
    
    private TableSegmentBinderContext mockTableBinderContext(final String databaseName, final String schemaName) {
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue("status"));
        column.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue(databaseName), new IdentifierValue(schemaName)),
                new IdentifierValue("t_order"), new IdentifierValue("status"), TableSourceType.PHYSICAL_TABLE));
        ProjectionSegment projection = new ColumnProjectionSegment(column);
        TableSegmentBinderContext result = mock(TableSegmentBinderContext.class);
        when(result.getProjectionSegments()).thenReturn(Collections.singleton(projection));
        return result;
    }
    
    private OwnerSegment createQualifiedOwnerSegment(final String qualifier, final String tableName) {
        OwnerSegment result = new OwnerSegment(0, 0, new IdentifierValue(tableName));
        result.setOwner(new OwnerSegment(0, 0, new IdentifierValue(qualifier)));
        return result;
    }
    
    @Test
    void assertGetNearestScopeTableBinderContextsWithDatabaseQualifiedOwnerInBothScopes() {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> fartherTableBinderContexts = LinkedHashMultimap.create();
        fartherTableBinderContexts.put(CaseInsensitiveString.of("t_order"), mockTableBinderContext("foo_db", "foo_db"));
        TableSegmentBinderContext nearerTableBinderContext = mockTableBinderContext("foo_db", "foo_db");
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> nearerTableBinderContexts = LinkedHashMultimap.create();
        nearerTableBinderContexts.put(CaseInsensitiveString.of("t_order"), nearerTableBinderContext);
        ScopedTableSegmentBinderContexts actual = new ScopedTableSegmentBinderContexts(fartherTableBinderContexts, nearerTableBinderContexts);
        assertThat(actual.getNearestScopeTableBinderContexts(createQualifiedOwnerSegment("foo_db", "t_order")), contains(nearerTableBinderContext));
    }
    
    @Test
    void assertGetNearestScopeTableBinderContextsWithSchemaQualifiedOwner() {
        TableSegmentBinderContext publicTableBinderContext = mockTableBinderContext("foo_db", "public");
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> fartherTableBinderContexts = LinkedHashMultimap.create();
        fartherTableBinderContexts.put(CaseInsensitiveString.of("t_order"), publicTableBinderContext);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> nearerTableBinderContexts = LinkedHashMultimap.create();
        nearerTableBinderContexts.put(CaseInsensitiveString.of("t_order"), mockTableBinderContext("foo_db", "foo_schema"));
        ScopedTableSegmentBinderContexts actual = new ScopedTableSegmentBinderContexts(fartherTableBinderContexts, nearerTableBinderContexts);
        assertThat(actual.getNearestScopeTableBinderContexts(createQualifiedOwnerSegment("public", "t_order")), contains(publicTableBinderContext));
    }
}
