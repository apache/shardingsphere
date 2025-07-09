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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.oracle.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.packages.PackageSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.type.TypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.statistics.OracleAssociateStatisticsStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.index.IndexAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.index.IndexTypeAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.packages.PackageAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.type.TypeAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.statistics.OracleAssociateStatisticsStatementTestCase;

/**
 * Associate Statistics statement assert for Oracle.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OracleAssociateStatisticsStatementAssert {
    
    /**
     * Assert associate statistics statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual associate statistics statement
     * @param expected expected associate statistics statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final OracleAssociateStatisticsStatement actual, final OracleAssociateStatisticsStatementTestCase expected) {
        assertTables(assertContext, actual, expected);
        assertColumns(assertContext, actual, expected);
        assertIndexes(assertContext, actual, expected);
        assertFunctions(assertContext, actual, expected);
        assertPackages(assertContext, actual, expected);
        assertTypes(assertContext, actual, expected);
        assertIndexTypes(assertContext, actual, expected);
    }
    
    private static void assertTables(final SQLCaseAssertContext assertContext, final OracleAssociateStatisticsStatement actual, final OracleAssociateStatisticsStatementTestCase expected) {
        if (null != expected.getTables()) {
            TableAssert.assertIs(assertContext, actual.getTables(), expected.getTables());
        }
    }
    
    private static void assertColumns(final SQLCaseAssertContext assertContext, final OracleAssociateStatisticsStatement actual, final OracleAssociateStatisticsStatementTestCase expected) {
        if (null != expected.getColumns()) {
            int count = 0;
            for (ColumnSegment each : actual.getColumns()) {
                ColumnAssert.assertIs(assertContext, each, expected.getColumns().get(count));
                count++;
            }
        }
    }
    
    private static void assertIndexes(final SQLCaseAssertContext assertContext, final OracleAssociateStatisticsStatement actual, final OracleAssociateStatisticsStatementTestCase expected) {
        if (null != expected.getIndexes()) {
            int count = 0;
            for (IndexSegment each : actual.getIndexes()) {
                IndexAssert.assertIs(assertContext, each, expected.getIndexes().get(count));
                count++;
            }
        }
    }
    
    private static void assertFunctions(final SQLCaseAssertContext assertContext, final OracleAssociateStatisticsStatement actual, final OracleAssociateStatisticsStatementTestCase expected) {
        if (null != expected.getFunctions()) {
            int count = 0;
            for (FunctionSegment each : actual.getFunctions()) {
                ExpressionAssert.assertFunction(assertContext, each, expected.getFunctions().get(count));
                count++;
            }
        }
    }
    
    private static void assertPackages(final SQLCaseAssertContext assertContext, final OracleAssociateStatisticsStatement actual, final OracleAssociateStatisticsStatementTestCase expected) {
        if (null != expected.getPackages()) {
            int count = 0;
            for (PackageSegment each : actual.getPackages()) {
                PackageAssert.assertIs(assertContext, each, expected.getPackages().get(count));
                count++;
            }
        }
    }
    
    private static void assertTypes(final SQLCaseAssertContext assertContext, final OracleAssociateStatisticsStatement actual, final OracleAssociateStatisticsStatementTestCase expected) {
        if (null != expected.getTypes()) {
            int count = 0;
            for (TypeSegment each : actual.getTypes()) {
                TypeAssert.assertIs(assertContext, each, expected.getTypes().get(count));
                count++;
            }
        }
    }
    
    private static void assertIndexTypes(final SQLCaseAssertContext assertContext, final OracleAssociateStatisticsStatement actual, final OracleAssociateStatisticsStatementTestCase expected) {
        if (null != expected.getIndexTypes()) {
            int count = 0;
            for (IndexTypeSegment each : actual.getIndexTypes()) {
                IndexTypeAssert.assertIs(assertContext, each, expected.getIndexTypes().get(count));
                count++;
            }
        }
    }
}
