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
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.ClusterStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.ClusterStatementHandler;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.index.IndexAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.ClusterStatementTestCase;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Cluster statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClusterStatementAssert {
    
    /**
     * Assert cluster statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual cluster statement
     * @param expected expected cluster statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ClusterStatement actual, final ClusterStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertIndex(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final ClusterStatement actual, final ClusterStatementTestCase expected) {
        Optional<SimpleTableSegment> tableSegment = ClusterStatementHandler.getSimpleTableSegment(actual);
        if (null == expected.getTable()) {
            assertFalse(assertContext.getText("Actual table segment should not exist"), tableSegment.isPresent());
        } else {
            assertTrue(assertContext.getText("Actual table segment should exist"), tableSegment.isPresent());
            TableAssert.assertIs(assertContext, tableSegment.get(), expected.getTable());
        }
    }
    
    private static void assertIndex(final SQLCaseAssertContext assertContext, final ClusterStatement actual, final ClusterStatementTestCase expected) {
        Optional<IndexSegment> indexSegment = ClusterStatementHandler.getIndexSegment(actual);
        if (null == expected.getIndex()) {
            assertFalse(assertContext.getText("Actual index segment should not exist"), indexSegment.isPresent());
        } else {
            assertTrue(assertContext.getText("Actual index segment should exist"), indexSegment.isPresent());
            IndexAssert.assertIs(assertContext, indexSegment.get(), expected.getIndex());
        }
    }
}
