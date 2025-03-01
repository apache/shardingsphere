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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.bound;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.identifier.IdentifierValueAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.bound.ExpectedColumnBoundInfo;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.bound.ExpectedTableSourceType;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Column bound assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnBoundAssert {
    
    /**
     * Assert actual column bound info is correct with expected column bound info.
     *
     * @param assertContext assert context
     * @param actual actual column bound info
     * @param expected expected column bound info
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ColumnSegmentBoundInfo actual, final ExpectedColumnBoundInfo expected) {
        if (null == expected) {
            return;
        }
        IdentifierValueAssert.assertIs(assertContext, actual.getOriginalDatabase(), expected.getOriginalDatabase(), "Bound Database");
        IdentifierValueAssert.assertIs(assertContext, actual.getOriginalSchema(), expected.getOriginalSchema(), "Bound Schema");
        IdentifierValueAssert.assertIs(assertContext, actual.getOriginalTable(), expected.getOriginalTable(), "Bound Table");
        IdentifierValueAssert.assertIs(assertContext, actual.getOriginalColumn(), expected.getOriginalColumn(), "Bound Column");
        assertTableSourceType(assertContext, actual, expected);
    }
    
    private static void assertTableSourceType(final SQLCaseAssertContext assertContext, final ColumnSegmentBoundInfo actual, final ExpectedColumnBoundInfo expected) {
        ExpectedTableSourceType expectedTableSourceType = expected.getTableSourceType();
        TableSourceType actualTableSourceType = actual.getTableSourceType();
        if (null == expectedTableSourceType) {
            assertNull(actualTableSourceType, assertContext.getText("Actual table source type should not exist."));
        } else {
            assertNotNull(actualTableSourceType, assertContext.getText("Actual table source type should exist."));
            assertThat(assertContext.getText(String.format("%s name assertion error: ", "Table Source Type")), actualTableSourceType.name(), is(expectedTableSourceType.getName()));
        }
    }
}
