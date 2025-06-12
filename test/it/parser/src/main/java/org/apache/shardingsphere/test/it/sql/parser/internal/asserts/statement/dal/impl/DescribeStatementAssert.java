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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.DescribeStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.DescribeStatementTestCase;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Describe statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DescribeStatementAssert {
    
    /**
     * Assert describe statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual explain statement
     * @param expected expected describe statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DescribeStatement actual, final DescribeStatementTestCase expected) {
        if (actual.getColumnWild().isPresent() && null != expected.getTable()) {
            assertExplainStatementColumnWild(assertContext, actual, expected);
        }
    }
    
    private static void assertExplainStatementColumnWild(final SQLCaseAssertContext assertContext, final DescribeStatement actual, final DescribeStatementTestCase expected) {
        if (actual.getTable().isPresent()) {
            TableAssert.assertIs(assertContext, actual.getTable().get(), expected.getTable());
            if (actual.getColumnWild().isPresent()) {
                ColumnAssert.assertIs(assertContext, actual.getColumnWild().get(), expected.getColumn());
            } else {
                assertFalse(actual.getColumnWild().isPresent(), assertContext.getText("Actual column wild should not exist."));
            }
        } else {
            assertFalse(actual.getTable().isPresent(), assertContext.getText("Actual table should not exist."));
        }
    }
}
