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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.comment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.comments.ExpectedComment;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;

import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Comment assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentAssert {
    
    /**
     * Assert comment is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual sql statement
     * @param expected expected statement test case
     */
    public static void assertComment(final SQLCaseAssertContext assertContext, final SQLStatement actual, final SQLParserTestCase expected) {
        if (expected.getComments().isEmpty()) {
            assertCommentIsEmpty(assertContext, actual);
        } else {
            assertCommentIsCorrect(assertContext, actual, expected);
        }
    }
    
    private static void assertCommentIsEmpty(final SQLCaseAssertContext assertContext, final SQLStatement actual) {
        if (actual instanceof AbstractSQLStatement) {
            assertTrue(assertContext.getText("Comment should be empty."), ((AbstractSQLStatement) actual).getCommentSegments().isEmpty());
        }
    }
    
    private static void assertCommentIsCorrect(final SQLCaseAssertContext assertContext, final SQLStatement actual, final SQLParserTestCase expected) {
        assertTrue(assertContext.getText("Comment should exist."), actual instanceof AbstractSQLStatement);
        assertThat(assertContext.getText("Comments size assertion error: "), ((AbstractSQLStatement) actual).getCommentSegments().size(), is(expected.getComments().size()));
        Iterator<CommentSegment> actualIterator = ((AbstractSQLStatement) actual).getCommentSegments().iterator();
        for (final ExpectedComment each : expected.getComments()) {
            assertThat(assertContext.getText("Comments assertion error: "), actualIterator.next().getText(),
                    is(each.getText()));
        }
    }
}
