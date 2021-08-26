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
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.function.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAssociateStatisticsStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.function.FunctionAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.index.IndexAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AssociateStatisticsStatementTestCase;

/**
 * Associate Statistics statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AssociateStatisticsStatementAssert {
    
    /**
     * Assert associate statistics statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual associate statistics statement
     * @param expected expected associate statistics statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final OracleAssociateStatisticsStatement actual, final AssociateStatisticsStatementTestCase expected) {
        assertTables(assertContext, actual, expected);
        assertColumns(assertContext, actual, expected);
        assertIndexes(assertContext, actual, expected);
        assertFunctions(assertContext, actual, expected);
    }
    
    private static void assertTables(final SQLCaseAssertContext assertContext, final OracleAssociateStatisticsStatement actual, final AssociateStatisticsStatementTestCase expected) {
        if (null != expected.getTables()) {
            TableAssert.assertIs(assertContext, actual.getTables(), expected.getTables());
        }
    }

    private static void assertColumns(final SQLCaseAssertContext assertContext, final OracleAssociateStatisticsStatement actual, final AssociateStatisticsStatementTestCase expected) {
        if (null != expected.getColumns()) {
            int count = 0;
            for (ColumnSegment each : actual.getColumns()) {
                ColumnAssert.assertIs(assertContext, each, expected.getColumns().get(count));
                count++;
            }
        }
    }
    
    private static void assertIndexes(final SQLCaseAssertContext assertContext, final OracleAssociateStatisticsStatement actual, final AssociateStatisticsStatementTestCase expected) {
        if (null != expected.getIndexes()) {
            int count = 0;
            for (IndexSegment each : actual.getIndexes()) {
                IndexAssert.assertIs(assertContext, each, expected.getIndexes().get(count));
                count++;
            }
        }
    }

    private static void assertFunctions(final SQLCaseAssertContext assertContext, final OracleAssociateStatisticsStatement actual, final AssociateStatisticsStatementTestCase expected) {
        if (null != expected.getFunctions()) {
            int count = 0;
            for (FunctionSegment each : actual.getFunctions()) {
                FunctionAssert.assertIs(assertContext, each, expected.getFunctions().get(count));
                count++;
            }
        }
    }
}
