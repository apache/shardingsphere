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

package org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiteralExpressionSegmentTest {
    
    @Test
    void assertIsNullLiteral() {
        assertTrue(new LiteralExpressionSegment(0, 3, null).isNullLiteral());
    }
    
    @Test
    void assertIsNullLiteralWithNullString() {
        assertFalse(new LiteralExpressionSegment(0, 5, "null").isNullLiteral());
    }
    
    @Test
    void assertIsNullLiteralWithEmptyString() {
        assertFalse(new LiteralExpressionSegment(0, 1, "").isNullLiteral());
    }
    
    @Test
    void assertGetTextWithNullValue() {
        assertNull(new LiteralExpressionSegment(0, 3, null).getText());
    }
    
    @Test
    void assertGetTextWithNullString() {
        assertThat(new LiteralExpressionSegment(0, 5, "null").getText(), is("null"));
    }
}
