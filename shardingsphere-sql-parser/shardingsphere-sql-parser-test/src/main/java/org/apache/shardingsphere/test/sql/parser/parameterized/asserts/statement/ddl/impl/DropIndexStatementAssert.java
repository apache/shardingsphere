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
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.DropIndexStatementHandler;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DropIndexStatementTestCase;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Drop index statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DropIndexStatementAssert {
    
    /**
     * Assert drop index statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual drop index statement
     * @param expected expected drop index statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DropIndexStatement actual, final DropIndexStatementTestCase expected) {
        assertTables(assertContext, actual, expected);
        assertIndex(assertContext, actual, expected);
    }
    
    private static void assertTables(final SQLCaseAssertContext assertContext, final DropIndexStatement actual, final DropIndexStatementTestCase expected) {
        Optional<SimpleTableSegment> simpleTableSegment = DropIndexStatementHandler.getSimpleTableSegment(actual);
        if (null != expected.getTable()) {
            assertTrue(assertContext.getText("Actual table segment should exist."), simpleTableSegment.isPresent());
            TableAssert.assertIs(assertContext, simpleTableSegment.get(), expected.getTable());
        } else {
            assertFalse(assertContext.getText("Actual table segment should not exist."), simpleTableSegment.isPresent());
        }
    }
    
    private static void assertIndex(final SQLCaseAssertContext assertContext, final DropIndexStatement actual, final DropIndexStatementTestCase expected) {
        // TODO should assert index for all databases(mysql and sqlserver do not parse index right now)
//        assertThat(assertContext.getText("Index size assertion error: "), actual.getIndexes().size(), is(expected.getIndexes().size()));
//        int count = 0;
//        for (IndexSegment each : actual.getIndexes()) {
//            IndexAssert.assertIs(assertContext, each, expected.getIndexes().get(count));
//            count++;
//        }
    }
}
