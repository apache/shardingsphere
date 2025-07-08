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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.sqlserver.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.ddl.statistics.SQLServerUpdateStatisticsStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.index.IndexAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.statistics.StatisticsStrategyAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.index.ExpectedIndex;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.sqlserver.statistics.SQLServerUpdateStatisticsStatementTestCase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *  Update statistics statement assert for SQLServer.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLServerUpdateStatisticsStatementAssert {
    
    /**
     * Assert update statistics statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual update statistics statement
     * @param expected expected update statistics statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SQLServerUpdateStatisticsStatement actual, final SQLServerUpdateStatisticsStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertIndex(assertContext, actual, expected);
        assertStrategy(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final SQLServerUpdateStatisticsStatement actual, final SQLServerUpdateStatisticsStatementTestCase expected) {
        if (null == expected.getTable()) {
            assertNull(actual.getTable(), assertContext.getText("Actual table segment should not exist."));
        } else {
            assertNotNull(actual.getTable(), assertContext.getText("Actual table segment should exist."));
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        }
    }
    
    private static void assertIndex(final SQLCaseAssertContext assertContext, final SQLServerUpdateStatisticsStatement actual, final SQLServerUpdateStatisticsStatementTestCase expected) {
        if (null == expected.getIndexes()) {
            assertNull(actual.getIndexes(), assertContext.getText("Actual index segment should not exist."));
        } else {
            int count = 0;
            assertNotNull(actual.getIndexes(), assertContext.getText("Actual index segment should exist."));
            for (ExpectedIndex index : expected.getIndexes()) {
                IndexAssert.assertIs(assertContext, actual.getIndexes().get(count), index);
                count++;
            }
        }
    }
    
    private static void assertStrategy(final SQLCaseAssertContext assertContext, final SQLServerUpdateStatisticsStatement actual, final SQLServerUpdateStatisticsStatementTestCase expected) {
        if (null == expected.getStrategy()) {
            assertNull(actual.getStrategy(), assertContext.getText("Actual strategy segment should not exist."));
        } else {
            assertNotNull(actual.getStrategy(), assertContext.getText("Actual strategy segment should exist."));
            StatisticsStrategyAssert.assertIs(assertContext, actual.getStrategy(), expected.getStrategy());
        }
    }
    
}
