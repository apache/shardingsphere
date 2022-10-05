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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

public final class ColumnExtractorTest {
    
    @Test
    public void assertExtractColumnSegments() {
        final List<ColumnSegment> list = new ArrayList<>();
        final ColumnSegment nameCol = new ColumnSegment(10, 13, new IdentifierValue("name"));
        final ColumnSegment pwnCol = new ColumnSegment(30, 32, new IdentifierValue("pwd"));
        final List<WhereSegment> wheres = Collections.singletonList(createWhereSegment(nameCol, pwnCol));
        ColumnExtractor.extractColumnSegments(list, wheres);
        assertThat(list.size(), is(2));
        assertThat(list, contains(nameCol, pwnCol));
    }
    
    private static WhereSegment createWhereSegment(final ColumnSegment col1, final ColumnSegment col2) {
        BinaryOperationExpression nameExpression = new BinaryOperationExpression(10, 24,
                col1, new LiteralExpressionSegment(18, 22, "LiLei"), "=", "name = 'LiLei'");
        BinaryOperationExpression pwdExpression = new BinaryOperationExpression(30, 44,
                col2, new LiteralExpressionSegment(40, 45, "123456"), "=", "pwd = '123456'");
        return new WhereSegment(0, 0, new BinaryOperationExpression(0, 0, nameExpression, pwdExpression, "AND", "name = 'LiLei' AND pwd = '123456'"));
    }
}
