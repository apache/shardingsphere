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
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.identifier.IdentifierValueAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.bound.ExpectedTableBoundInfo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Table bound assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableBoundAssert {
    
    /**
     * Assert actual table bound info is correct with expected table bound info.
     *
     * @param assertContext assert context
     * @param actual actual table bound info
     * @param expected expected table bound info
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final TableSegmentBoundInfo actual, final ExpectedTableBoundInfo expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual table bound should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual table bound should exist."));
            IdentifierValueAssert.assertIs(assertContext, actual.getOriginalDatabase(), expected.getOriginalDatabase(), "Bound Database");
            IdentifierValueAssert.assertIs(assertContext, actual.getOriginalSchema(), expected.getOriginalSchema(), "Bound Schema");
        }
    }
}
