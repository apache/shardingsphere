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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.comment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.comments.ExpectedComment;

import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comment assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentAssert {
    
    /**
     * Assert comment is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual SQL statement
     * @param expected expected statement test case
     */
    public static void assertComment(final SQLCaseAssertContext assertContext, final SQLStatement actual, final SQLParserTestCase expected) {
        if (expected.getComments().isEmpty()) {
            assertEmptyComment(assertContext, actual);
        } else {
            assertCorrectComment(assertContext, actual, expected);
        }
    }
    
    private static void assertEmptyComment(final SQLCaseAssertContext assertContext, final SQLStatement actual) {
        assertTrue(actual.getComments().isEmpty(), assertContext.getText("Comment should be empty."));
    }
    
    private static void assertCorrectComment(final SQLCaseAssertContext assertContext, final SQLStatement actual, final SQLParserTestCase expected) {
        assertThat(assertContext.getText("Comment should exist."), actual, isA(SQLStatement.class));
        assertThat(assertContext.getText("Comments size assertion error: "), actual.getComments().size(), is(expected.getComments().size()));
        Iterator<CommentSegment> actualIterator = actual.getComments().iterator();
        for (ExpectedComment each : expected.getComments()) {
            assertThat(assertContext.getText("Comments assertion error: "), actualIterator.next().getText(), is(each.getText()));
        }
    }
}
