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

package org.apache.shardingsphere.infra.binder.segment.where;

import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class WhereSegmentBinderTest {
    
    @Mock
    private WhereSegmentBinder whereSegmentBinder;
    
    @Test
    void assertBind() {
        SQLStatementBinderContext sqlStatementBinderContext = mock(SQLStatementBinderContext.class);
        WhereSegment expectedWhereSegment = new WhereSegment(1, 2, mock(ExpressionSegment.class));
        Map<String, TableSegmentBinderContext> tableBinderContexts = new HashMap<>();
        Map<String, TableSegmentBinderContext> outerTableBinderContexts = new HashMap<>();
        whereSegmentBinder = mock(WhereSegmentBinder.class);
        doReturn(expectedWhereSegment).when(whereSegmentBinder);
        WhereSegment actualWhereSegment = WhereSegmentBinder.bind(expectedWhereSegment, sqlStatementBinderContext, tableBinderContexts, outerTableBinderContexts);
        Assertions.assertEquals(expectedWhereSegment.getStopIndex(), actualWhereSegment.getStopIndex());
        Assertions.assertEquals(expectedWhereSegment.getStartIndex(), actualWhereSegment.getStartIndex());
        Assertions.assertEquals(expectedWhereSegment.getExpr(), actualWhereSegment.getExpr());
    }
}
