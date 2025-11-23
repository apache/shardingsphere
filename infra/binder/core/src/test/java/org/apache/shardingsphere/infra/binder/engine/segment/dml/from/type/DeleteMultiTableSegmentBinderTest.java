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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(TableSegmentBinder.class)
class DeleteMultiTableSegmentBinderTest {
    
    @SuppressWarnings("unchecked")
    @Test
    void assertBind() {
        DeleteMultiTableSegment segment = new DeleteMultiTableSegment();
        segment.setStartIndex(10);
        segment.setStopIndex(20);
        SimpleTableSegment firstDeleteTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        SimpleTableSegment secondDeleteTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_item")));
        segment.getActualDeleteTables().add(firstDeleteTable);
        segment.getActualDeleteTables().add(secondDeleteTable);
        TableSegment relationTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("relation_table")));
        segment.setRelationTable(relationTable);
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        TableSegment boundRelationTable = mock(TableSegment.class);
        when(TableSegmentBinder.bind(eq(relationTable), eq(binderContext), eq(tableBinderContexts), any(Multimap.class))).thenReturn(boundRelationTable);
        DeleteMultiTableSegment actual = DeleteMultiTableSegmentBinder.bind(segment, binderContext, tableBinderContexts);
        assertThat(actual.getStartIndex(), is(10));
        assertThat(actual.getStopIndex(), is(20));
        assertThat(actual.getActualDeleteTables(), is(segment.getActualDeleteTables()));
        assertThat(actual.getRelationTable(), is(boundRelationTable));
    }
}
