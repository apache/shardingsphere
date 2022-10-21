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

package org.apache.shardingsphere.sql.parser.sql.common.util;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class ColumnExtractorParameterizedTest {
    
    private static Collection<ExpressionSegment> testUnits = new LinkedList<>();
    
    private static final ColumnSegment COLUMN_SEGMENT = new ColumnSegment(35, 42, new IdentifierValue("order_item_id"));
    
    static {
        testUnits.add(new BinaryOperationExpression(0, 0, COLUMN_SEGMENT, null, null, null));
        testUnits.add(new InExpression(0, 0, COLUMN_SEGMENT, null, false));
        testUnits.add(new BetweenExpression(0, 0, COLUMN_SEGMENT, null, null, false));
    }
    
    private final ExpressionSegment expression;
    
    @Parameters(name = "{0}")
    public static Collection<ExpressionSegment> getTestParameters() {
        return testUnits;
    }
    
    @Test
    public void assertExtract() {
        Collection<ColumnSegment> columnSegments = ColumnExtractor.extract(expression);
        assertThat(columnSegments.size(), is(1));
        assertThat(columnSegments.iterator().next(), is(COLUMN_SEGMENT));
    }
}
