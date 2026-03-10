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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.order.item;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexOrderByItemSegmentBinderTest {
    
    @Test
    void assertBind() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("user_name"));
        columnSegment.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("")),
                new IdentifierValue("t_user"), new IdentifierValue("user_name"), TableSourceType.PHYSICAL_TABLE));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        tableBinderContexts.put(CaseInsensitiveString.of(""), new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(columnSegment)), TableSourceType.MIXED_TABLE));
        IndexOrderByItemSegment actual = IndexOrderByItemSegmentBinder.bind(new IndexOrderByItemSegment(6, 6, 1, OrderDirection.ASC, NullsOrderType.FIRST), tableBinderContexts);
        assertTrue(actual.getBoundColumn().isPresent());
        assertThat(actual.getBoundColumn().get().getColumnBoundInfo().getOriginalTable().getValue(), is("t_user"));
        assertThat(actual.getBoundColumn().get().getColumnBoundInfo().getOriginalColumn().getValue(), is("user_name"));
    }
    
    @Test
    void assertBindWithoutBoundColumn() {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        tableBinderContexts.put(CaseInsensitiveString.of(""), new SimpleTableSegmentBinderContext(Collections.emptyList(), TableSourceType.MIXED_TABLE));
        IndexOrderByItemSegment actual = IndexOrderByItemSegmentBinder.bind(new IndexOrderByItemSegment(6, 6, 1, OrderDirection.ASC, NullsOrderType.FIRST), tableBinderContexts);
        assertFalse(actual.getBoundColumn().isPresent());
    }
}
