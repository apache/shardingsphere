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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dal.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.schema.SchemaAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowIndexStatementTestCase;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.dialect.mysql.ShowIndexStatement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Show index statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowIndexStatementAssert {
    
    /**
     * Assert show index statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual show index statement
     * @param expected expected show index statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ShowIndexStatement actual, final ShowIndexStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertSchema(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final ShowIndexStatement actual, final ShowIndexStatementTestCase expected) {
        if (null != expected.getTable()) {
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        } else {
            assertNull(assertContext.getText("Actual table segment should not exist."), actual.getTable());
        }
    }
    
    private static void assertSchema(final SQLCaseAssertContext assertContext, final ShowIndexStatement actual, final ShowIndexStatementTestCase expected) {
        if (null != expected.getSchema()) {
            assertTrue(assertContext.getText("Actual schema segment should exist."), actual.getSchema().isPresent());
            SchemaAssert.assertIs(assertContext, actual.getSchema().get(), expected.getSchema());
        } else {
            assertFalse(assertContext.getText("Actual schema segment should not exist."), actual.getSchema().isPresent());
        }
    }
}
