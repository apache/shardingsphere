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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.value;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.ExpectedIdentifierSQLSegment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Identifier value assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IdentifierValueAssert {
    
    /**
     * Assert actual identifier value is correct with expected identifier.
     *
     * @param assertContext assert context
     * @param actual actual identifier value
     * @param expected expected identifier
     * @param type assert identifier value type
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final IdentifierValue actual,
                                final ExpectedIdentifierSQLSegment expected, final String type) {
        assertThat(assertContext.getText(String.format("%s name assertion error: ", type)),
                actual.getValue(), is(expected.getName()));
        assertThat(assertContext.getText(String.format("%s start delimiter assertion error: ", type)),
                actual.getQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertContext.getText(String.format("%s end delimiter assertion error: ", type)),
                actual.getQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
    }
}
