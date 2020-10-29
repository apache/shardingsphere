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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.AlterIndexStatementHandler;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterIndexStatementTestCase;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Alter index statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterIndexStatementAssert {
    
    /**
     * Assert alter index statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual alter index statement
     * @param expected expected alter index statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterIndexStatement actual, final AlterIndexStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertIndex(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final AlterIndexStatement actual, final AlterIndexStatementTestCase expected) {
        Optional<SimpleTableSegment> tableSegment = AlterIndexStatementHandler.getSimpleTableSegment(actual);
        if (null != expected.getTable()) {
            assertTrue(assertContext.getText("Actual table segment should exist."), tableSegment.isPresent());
            TableAssert.assertIs(assertContext, tableSegment.get(), expected.getTable());
        } else {
            assertFalse(assertContext.getText("Actual table segment should not exist."), tableSegment.isPresent());
        }
    }
    
    private static void assertIndex(final SQLCaseAssertContext assertContext, final AlterIndexStatement actual, final AlterIndexStatementTestCase expected) {
        // TODO should assert index for all databases(mysql and sqlserver do not parse index right now)
//        IndexAssert.assertIs(assertContext, actual.getIndex(), expected.getIndex());
    }
}
