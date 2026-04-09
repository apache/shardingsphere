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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowCreateLoadStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.identifier.IdentifierValueAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowCreateLoadStatementTestCase;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Show create load statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisShowCreateLoadStatementAssert {
    
    /**
     * Assert show create load statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show create load statement
     * @param expected expected show create load statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisShowCreateLoadStatement actual, final DorisShowCreateLoadStatementTestCase expected) {
        assertNotNull(actual.getLoadName(), assertContext.getText("Actual load name should exist."));
        assertNotNull(expected.getLoadName(), assertContext.getText("Expected load name should exist."));
        IdentifierValueAssert.assertIs(assertContext, actual.getLoadName().getIdentifier(), expected.getLoadName(), "Load name");
        SQLSegmentAssert.assertIs(assertContext, actual.getLoadName(), expected.getLoadName());
    }
}
