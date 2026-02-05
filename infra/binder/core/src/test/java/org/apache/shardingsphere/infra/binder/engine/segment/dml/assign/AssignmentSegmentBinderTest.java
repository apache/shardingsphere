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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.assign;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;

class AssignmentSegmentBinderTest {
    
    @Test
    void assertBindAssignmentSegment() {
        Collection<ColumnAssignmentSegment> assignments = new LinkedList<>();
        ColumnSegment boundOrderIdColumn = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        boundOrderIdColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_db")),
                new IdentifierValue("t_order"), new IdentifierValue("order_id"), TableSourceType.PHYSICAL_TABLE));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        assignments.add(new ColumnAssignmentSegment(0, 0, Collections.singletonList(columnSegment), new LiteralExpressionSegment(0, 0, 1)));
        SetAssignmentSegment setAssignmentSegment = new SetAssignmentSegment(0, 0, assignments);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        tableBinderContexts.put(CaseInsensitiveString.of("t_order"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundOrderIdColumn)), TableSourceType.PHYSICAL_TABLE));
        SetAssignmentSegment actual = AssignmentSegmentBinder.bind(setAssignmentSegment, mock(SQLStatementBinderContext.class), tableBinderContexts, LinkedHashMultimap.create());
        assertThat(actual, not(setAssignmentSegment));
        assertThat(actual.getAssignments().iterator().next(), not(setAssignmentSegment.getAssignments().iterator().next()));
        assertThat(actual.getAssignments().iterator().next().getColumns().iterator().next().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
    }
}
