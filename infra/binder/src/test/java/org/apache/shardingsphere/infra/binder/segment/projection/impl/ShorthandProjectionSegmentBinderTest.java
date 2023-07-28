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

package org.apache.shardingsphere.infra.binder.segment.projection.impl;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ShorthandProjectionSegmentBinderTest {
    
    @Test
    void assertBindWithOwner() {
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 0);
        shorthandProjectionSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("o")));
        Map<String, TableSegmentBinderContext> tableBinderContexts = new CaseInsensitiveMap<>();
        ColumnProjectionSegment invisibleColumn = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("status")));
        invisibleColumn.setVisible(false);
        tableBinderContexts.put("o", new TableSegmentBinderContext(Arrays.asList(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))), invisibleColumn)));
        ShorthandProjectionSegment actual = ShorthandProjectionSegmentBinder.bind(shorthandProjectionSegment, mock(TableSegment.class), tableBinderContexts);
        assertThat(actual.getActualProjectionSegments().size(), is(1));
        ProjectionSegment visibleColumn = actual.getActualProjectionSegments().iterator().next();
        assertThat(visibleColumn.getColumnLabel(), is("order_id"));
        assertTrue(visibleColumn.isVisible());
    }
    
    @Test
    void assertBindWithoutOwnerForSimpleTableSegment() {
        Map<String, TableSegmentBinderContext> tableBinderContexts = new CaseInsensitiveMap<>();
        ColumnProjectionSegment invisibleColumn = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("status")));
        invisibleColumn.setVisible(false);
        tableBinderContexts.put("o", new TableSegmentBinderContext(Arrays.asList(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))), invisibleColumn)));
        SimpleTableSegment boundedTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        boundedTableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("o")));
        ShorthandProjectionSegment actual = ShorthandProjectionSegmentBinder.bind(new ShorthandProjectionSegment(0, 0), boundedTableSegment, tableBinderContexts);
        assertThat(actual.getActualProjectionSegments().size(), is(1));
        ProjectionSegment visibleColumn = actual.getActualProjectionSegments().iterator().next();
        assertThat(visibleColumn.getColumnLabel(), is("order_id"));
        assertTrue(visibleColumn.isVisible());
    }
    
    @Test
    void assertBindWithoutOwnerForSubqueryTableSegment() {
        Map<String, TableSegmentBinderContext> tableBinderContexts = new CaseInsensitiveMap<>();
        ColumnProjectionSegment invisibleColumn = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("status")));
        invisibleColumn.setVisible(false);
        tableBinderContexts.put("o", new TableSegmentBinderContext(Arrays.asList(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))), invisibleColumn)));
        SubqueryTableSegment boundedTableSegment = new SubqueryTableSegment(new SubquerySegment(0, 0, mock(MySQLSelectStatement.class)));
        boundedTableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("o")));
        ShorthandProjectionSegment actual = ShorthandProjectionSegmentBinder.bind(new ShorthandProjectionSegment(0, 0), boundedTableSegment, tableBinderContexts);
        assertThat(actual.getActualProjectionSegments().size(), is(1));
        ProjectionSegment visibleColumn = actual.getActualProjectionSegments().iterator().next();
        assertThat(visibleColumn.getColumnLabel(), is("order_id"));
        assertTrue(visibleColumn.isVisible());
    }
    
    @Test
    void assertBindWithoutOwnerForJoinTableSegment() {
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 0);
        JoinTableSegment boundedTableSegment = new JoinTableSegment();
        boundedTableSegment.getJoinTableProjectionSegments().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        ShorthandProjectionSegment actual = ShorthandProjectionSegmentBinder.bind(shorthandProjectionSegment, boundedTableSegment, Collections.emptyMap());
        assertThat(actual.getActualProjectionSegments().size(), is(1));
        ProjectionSegment visibleColumn = actual.getActualProjectionSegments().iterator().next();
        assertThat(visibleColumn.getColumnLabel(), is("order_id"));
        assertTrue(visibleColumn.isVisible());
    }
}
