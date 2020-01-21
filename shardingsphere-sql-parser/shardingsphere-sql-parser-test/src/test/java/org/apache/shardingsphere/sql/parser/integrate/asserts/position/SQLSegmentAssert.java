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

package org.apache.shardingsphere.sql.parser.integrate.asserts.position;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.generic.ExpectedSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.SQLSegment;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *  SQL segment assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLSegmentAssert {
    
    /**
     * Assert generic attributes of actual SQL segment are same with expected SQL segment.
     * 
     * @param assertMessage assert message
     * @param actual actual SQL segment
     * @param expected expected SQL segment
     * @param sqlCaseType SQL case type
     */
    public static void assertIs(final SQLStatementAssertMessage assertMessage, final SQLSegment actual, final ExpectedSegment expected, final SQLCaseType sqlCaseType) {
        assertStartIndex(assertMessage, actual, expected, sqlCaseType);
        assertStopIndex(assertMessage, actual, expected, sqlCaseType);
    }
    
    private static void assertStartIndex(final SQLStatementAssertMessage assertMessage, final SQLSegment actual, final ExpectedSegment expected, final SQLCaseType sqlCaseType) {
        int expectedStartIndex = SQLCaseType.Literal == sqlCaseType && null != expected.getLiteralStartIndex() ? expected.getLiteralStartIndex() : expected.getStartIndex();
        assertThat(assertMessage.getText(String.format("`%s`'s start index assertion error: ", actual.getClass())), actual.getStartIndex(), is(expectedStartIndex));
    }
    
    private static void assertStopIndex(final SQLStatementAssertMessage assertMessage, final SQLSegment actual, final ExpectedSegment expected, final SQLCaseType sqlCaseType) {
        int expectedStopIndex = SQLCaseType.Literal == sqlCaseType && null != expected.getLiteralStopIndex() ? expected.getLiteralStopIndex() : expected.getStopIndex();
        assertThat(assertMessage.getText(String.format("`%s`'s stop index assertion error: ", actual.getClass())), actual.getStopIndex(), is(expectedStopIndex));
    }
}
