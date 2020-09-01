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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dcl.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.DenyUserStatementTestCase;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DenyUserStatement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Deny user statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DenyUserStatementAssert {
    
    /**
     * Assert deny user statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual deny user statement
     * @param expected expected deny user statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DenyUserStatement actual, final DenyUserStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final DenyUserStatement actual, final DenyUserStatementTestCase expected) {
        if (null != expected.getTable()) {
            assertNotNull(assertContext.getText("Actual table segment should exist."), actual.getTable());
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        } else {
            assertNull(assertContext.getText("Actual table segment should not exist."), actual.getTable());
        }
    }
}
