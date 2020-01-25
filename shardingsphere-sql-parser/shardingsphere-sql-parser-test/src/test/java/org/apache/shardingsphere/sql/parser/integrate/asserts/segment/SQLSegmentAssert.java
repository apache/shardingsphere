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

package org.apache.shardingsphere.sql.parser.integrate.asserts.segment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.generic.ExpectedSQLSegment;
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
     */
    public static void assertIs(final SQLCaseAssertMessage assertMessage, final SQLSegment actual, final ExpectedSQLSegment expected) {
        assertStartIndex(assertMessage, actual, expected);
        assertStopIndex(assertMessage, actual, expected);
    }
    
    private static void assertStartIndex(final SQLCaseAssertMessage assertMessage, final SQLSegment actual, final ExpectedSQLSegment expected) {
        int expectedStartIndex = SQLCaseType.Literal == assertMessage.getSqlCaseType() && null != expected.getLiteralStartIndex() ? expected.getLiteralStartIndex() : expected.getStartIndex();
        assertThat(assertMessage.getText(String.format("`%s`'s start index assertion error: ", actual.getClass().getSimpleName())), actual.getStartIndex(), is(expectedStartIndex));
    }
    
    private static void assertStopIndex(final SQLCaseAssertMessage assertMessage, final SQLSegment actual, final ExpectedSQLSegment expected) {
        int expectedStopIndex = SQLCaseType.Literal == assertMessage.getSqlCaseType() && null != expected.getLiteralStopIndex() ? expected.getLiteralStopIndex() : expected.getStopIndex();
        assertThat(assertMessage.getText(String.format("`%s`'s stop index assertion error: ", actual.getClass().getSimpleName())), actual.getStopIndex(), is(expectedStopIndex));
    }
}
