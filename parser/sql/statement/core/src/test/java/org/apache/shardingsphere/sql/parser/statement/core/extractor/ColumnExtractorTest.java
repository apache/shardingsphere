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

package org.apache.shardingsphere.sql.parser.statement.core.extractor;

import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ColumnExtractorTest {
    
    @Test
    void assertExtractColumnSegments() {
        Collection<ColumnSegment> columnSegments = ColumnExtractor.extractColumnSegments(createWhereSegments());
        assertThat(columnSegments.size(), is(2));
        Iterator<ColumnSegment> iterator = columnSegments.iterator();
        ColumnSegment firstColumn = iterator.next();
        assertThat(firstColumn.getIdentifier().getValue(), is("name"));
        ColumnSegment secondColumn = iterator.next();
        assertThat(secondColumn.getIdentifier().getValue(), is("pwd"));
    }
    
    private Collection<WhereSegment> createWhereSegments() {
        BinaryOperationExpression leftExpression = new BinaryOperationExpression(10, 24,
                new ColumnSegment(10, 13, new IdentifierValue("name")), new LiteralExpressionSegment(18, 22, "LiLei"), "=", "name = 'LiLei'");
        BinaryOperationExpression rightExpression = new BinaryOperationExpression(30, 44,
                new ColumnSegment(30, 32, new IdentifierValue("pwd")), new LiteralExpressionSegment(40, 45, "123456"), "=", "pwd = '123456'");
        return Collections.singleton(new WhereSegment(0, 0, new BinaryOperationExpression(0, 0, leftExpression, rightExpression, "AND", "name = 'LiLei' AND pwd = '123456'")));
    }
}
