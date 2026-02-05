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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ShorthandProjectionSegmentBinderTest {
    
    @Test
    void assertBindWithOwner() {
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 0);
        shorthandProjectionSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("o")));
        ColumnProjectionSegment invisibleColumn = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("status")));
        invisibleColumn.setVisible(false);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        tableBinderContexts.put(CaseInsensitiveString.of("o"),
                new SimpleTableSegmentBinderContext(Arrays.asList(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))), invisibleColumn),
                        TableSourceType.PHYSICAL_TABLE));
        ShorthandProjectionSegment actual = ShorthandProjectionSegmentBinder.bind(shorthandProjectionSegment, mock(TableSegment.class), tableBinderContexts);
        assertThat(actual.getActualProjectionSegments().size(), is(1));
        ProjectionSegment visibleColumn = actual.getActualProjectionSegments().iterator().next();
        assertThat(visibleColumn.getColumnLabel(), is("order_id"));
        assertTrue(visibleColumn.isVisible());
    }
    
    @Test
    void assertBindWithoutOwnerForSimpleTableSegment() {
        ColumnProjectionSegment invisibleColumn = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("status")));
        invisibleColumn.setVisible(false);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        tableBinderContexts.put(CaseInsensitiveString.of("o"),
                new SimpleTableSegmentBinderContext(Arrays.asList(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))), invisibleColumn),
                        TableSourceType.PHYSICAL_TABLE));
        SimpleTableSegment boundTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        boundTableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("o")));
        ShorthandProjectionSegment actual = ShorthandProjectionSegmentBinder.bind(new ShorthandProjectionSegment(0, 0), boundTableSegment, tableBinderContexts);
        assertThat(actual.getActualProjectionSegments().size(), is(1));
        ProjectionSegment visibleColumn = actual.getActualProjectionSegments().iterator().next();
        assertThat(visibleColumn.getColumnLabel(), is("order_id"));
        assertTrue(visibleColumn.isVisible());
    }
    
    @Test
    void assertBindWithoutOwnerForSubqueryTableSegment() {
        ColumnProjectionSegment invisibleColumn = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("status")));
        invisibleColumn.setVisible(false);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        tableBinderContexts.put(CaseInsensitiveString.of("o"),
                new SimpleTableSegmentBinderContext(Arrays.asList(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))), invisibleColumn),
                        TableSourceType.PHYSICAL_TABLE));
        SubqueryTableSegment boundTableSegment = new SubqueryTableSegment(0, 0, new SubquerySegment(0, 0, mock(SelectStatement.class), ""));
        boundTableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("o")));
        ShorthandProjectionSegment actual = ShorthandProjectionSegmentBinder.bind(new ShorthandProjectionSegment(0, 0), boundTableSegment, tableBinderContexts);
        assertThat(actual.getActualProjectionSegments().size(), is(1));
        ProjectionSegment visibleColumn = actual.getActualProjectionSegments().iterator().next();
        assertThat(visibleColumn.getColumnLabel(), is("order_id"));
        assertTrue(visibleColumn.isVisible());
    }
    
    @Test
    void assertBindWithoutOwnerForJoinTableSegment() {
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 0);
        JoinTableSegment boundTableSegment = new JoinTableSegment();
        boundTableSegment.getDerivedJoinTableProjectionSegments().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        ShorthandProjectionSegment actual = ShorthandProjectionSegmentBinder.bind(shorthandProjectionSegment, boundTableSegment, LinkedHashMultimap.create());
        assertThat(actual.getActualProjectionSegments().size(), is(1));
        ProjectionSegment visibleColumn = actual.getActualProjectionSegments().iterator().next();
        assertThat(visibleColumn.getColumnLabel(), is("order_id"));
        assertTrue(visibleColumn.isVisible());
    }
}
