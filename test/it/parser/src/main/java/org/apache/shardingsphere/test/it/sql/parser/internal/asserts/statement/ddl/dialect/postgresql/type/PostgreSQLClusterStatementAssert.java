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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLClusterStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.index.IndexAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLClusterStatementTestCase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cluster statement assert for PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLClusterStatementAssert {
    
    /**
     * Assert cluster statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual cluster statement
     * @param expected expected cluster statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final PostgreSQLClusterStatement actual, final PostgreSQLClusterStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertIndex(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final PostgreSQLClusterStatement actual, final PostgreSQLClusterStatementTestCase expected) {
        Optional<SimpleTableSegment> tableSegment = actual.getSimpleTable();
        if (null == expected.getTable()) {
            assertFalse(tableSegment.isPresent(), assertContext.getText("Actual table segment should not exist."));
        } else {
            assertTrue(tableSegment.isPresent(), assertContext.getText("Actual table segment should exist."));
            TableAssert.assertIs(assertContext, tableSegment.get(), expected.getTable());
        }
    }
    
    private static void assertIndex(final SQLCaseAssertContext assertContext, final PostgreSQLClusterStatement actual, final PostgreSQLClusterStatementTestCase expected) {
        Optional<IndexSegment> indexSegment = actual.getIndex();
        if (null == expected.getIndex()) {
            assertFalse(indexSegment.isPresent(), assertContext.getText("Actual index segment should not exist."));
        } else {
            assertTrue(indexSegment.isPresent(), assertContext.getText("Actual index segment should exist."));
            IndexAssert.assertIs(assertContext, indexSegment.get(), expected.getIndex());
        }
    }
}
