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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.from;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class TableSegmentBinderTest {
    
    @Test
    void assertBindFunctionTableWithoutAlias() {
        FunctionSegment function = new FunctionSegment(7, 27, "generate_series", "generate_series(1, 2)");
        function.getParameters().add(new LiteralExpressionSegment(23, 23, 1));
        function.getParameters().add(new LiteralExpressionSegment(26, 26, 2));
        FunctionTableSegment table = new FunctionTableSegment(7, 27, function);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> contexts = LinkedHashMultimap.create();
        FunctionTableSegment actual = (FunctionTableSegment) TableSegmentBinder.bind(table, mock(SQLStatementBinderContext.class), contexts, LinkedHashMultimap.create());
        assertThat(actual.getAliasName(), is(Optional.empty()));
        assertThat(actual.getTableFunction().getText(), is("generate_series(1, 2)"));
        assertThat(((FunctionSegment) actual.getTableFunction()).getParameters().size(), is(2));
        assertTrue(contexts.containsKey(CaseInsensitiveString.of("generate_series(1, 2)")));
    }
    
    @Test
    void assertBindFunctionTableWithColumns() {
        FunctionTableSegment functionTableSegment = new FunctionTableSegment(67, 127, new FunctionSegment(80, 98, "explode", "explode(type_array)"));
        functionTableSegment.setAlias(new AliasSegment(100, 113, new IdentifierValue("exploded_array")));
        functionTableSegment.getColumns().add(new ColumnSegment(118, 127, new IdentifierValue("item_value")));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        TableSegment actualSegment = TableSegmentBinder.bind(functionTableSegment, mock(SQLStatementBinderContext.class), tableBinderContexts, LinkedHashMultimap.create());
        assertThat(actualSegment, isA(FunctionTableSegment.class));
        assertThat(actualSegment.getStartIndex(), is(67));
        assertThat(actualSegment.getStopIndex(), is(127));
        assertThat(actualSegment.getAliasName(), is(Optional.of("exploded_array")));
        assertThat(((FunctionTableSegment) actualSegment).getColumns(), is(functionTableSegment.getColumns()));
        assertThat(((FunctionTableSegment) actualSegment).getTableFunction().getText(), is("explode(type_array)"));
        assertTrue(tableBinderContexts.containsKey(CaseInsensitiveString.of("exploded_array")));
        TableSegmentBinderContext actualBinderContext = tableBinderContexts.get(CaseInsensitiveString.of("exploded_array")).iterator().next();
        Optional<ProjectionSegment> actualProjection = actualBinderContext.findProjectionSegmentByColumnLabel("item_value");
        assertTrue(actualProjection.isPresent());
        assertThat(actualProjection.get(), isA(ColumnProjectionSegment.class));
        assertThat(actualProjection.get().getColumnLabel(), is("item_value"));
    }
}
