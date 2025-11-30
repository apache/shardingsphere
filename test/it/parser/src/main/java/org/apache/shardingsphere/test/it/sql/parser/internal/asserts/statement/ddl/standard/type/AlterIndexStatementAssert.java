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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.standard.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.AlterIndexStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.index.IndexAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.index.AlterIndexStatementTestCase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Optional<SimpleTableSegment> tableSegment = actual.getSimpleTable();
        if (null == expected.getTable()) {
            assertFalse(tableSegment.isPresent(), assertContext.getText("Actual table segment should not exist."));
        } else {
            assertTrue(tableSegment.isPresent(), assertContext.getText("Actual table segment should exist."));
            TableAssert.assertIs(assertContext, tableSegment.get(), expected.getTable());
        }
    }
    
    private static void assertIndex(final SQLCaseAssertContext assertContext, final AlterIndexStatement actual, final AlterIndexStatementTestCase expected) {
        if (actual.getIndex().isPresent()) {
            IndexAssert.assertIs(assertContext, actual.getIndex().get(), expected.getIndex());
        }
    }
}
