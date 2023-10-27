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

package org.apache.shardingsphere.infra.binder.segment.expression.impl;

import org.apache.shardingsphere.infra.binder.enums.SegmentType;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

class InExpressionBinderTest {

    @InjectMocks
    private InExpressionBinder subject;

    @Test
    void bindTest() {
        Map<String, TableSegmentBinderContext> tableBinderContexts = new LinkedHashMap<>();
        InExpression actual = subject.bind(mock(InExpression.class), mock(SegmentType.class), mock(SQLStatementBinderContext.class), tableBinderContexts);
        Assertions.assertInstanceOf(InExpression.class, actual);
    }
}
