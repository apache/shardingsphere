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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.join.OuterJoinExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class OuterJoinExpressionBinderTest {
    
    @Test
    void assertBindOuterJoinExpression() {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        ColumnSegment boundOrderIdColumn = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        TableSegmentBoundInfo tableBoundInfo = new TableSegmentBoundInfo(new IdentifierValue("t_order"), new IdentifierValue("order_id"));
        boundOrderIdColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(tableBoundInfo,
                new IdentifierValue("t_order"), new IdentifierValue("order_id"), TableSourceType.PHYSICAL_TABLE));
        tableBinderContexts.put(CaseInsensitiveString.of("t_order"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundOrderIdColumn)), TableSourceType.PHYSICAL_TABLE));
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        column.setOwner(new OwnerSegment(0, 0, new IdentifierValue("t_order")));
        OuterJoinExpression originalExpression = new OuterJoinExpression(0, 0, column, "+", "t_order.order_id(+)");
        OuterJoinExpression actual = OuterJoinExpressionBinder.bind(
                originalExpression, SegmentType.PREDICATE, mock(SQLStatementBinderContext.class), tableBinderContexts, LinkedHashMultimap.create());
        assertThat(actual.getStartIndex(), is(originalExpression.getStartIndex()));
        assertThat(actual.getStopIndex(), is(originalExpression.getStopIndex()));
        assertThat(actual.getColumnName().getIdentifier().getValue(), is("order_id"));
        assertThat(actual.getJoinOperator(), is("+"));
        assertThat(actual.getText(), is("t_order.order_id(+)"));
    }
}
