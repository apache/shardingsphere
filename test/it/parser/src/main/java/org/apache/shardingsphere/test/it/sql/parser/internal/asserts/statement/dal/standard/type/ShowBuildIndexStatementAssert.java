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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.standard.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowBuildIndexStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.database.DatabaseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.limit.LimitClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.orderby.OrderByClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.where.WhereClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowBuildIndexStatementTestCase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Show build index statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowBuildIndexStatementAssert {
    
    /**
     * Assert show build index statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show build index statement
     * @param expected expected show build index statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ShowBuildIndexStatement actual, final ShowBuildIndexStatementTestCase expected) {
        if (null != expected.getDatabase()) {
            assertNotNull(actual.getDatabase(), assertContext.getText("Actual database should exist."));
            DatabaseAssert.assertIs(assertContext, actual.getDatabase().get(), expected.getDatabase());
        } else {
            assertNull(actual.getDatabase().orElse(null), assertContext.getText("Actual database should not exist."));
        }
        if (null != expected.getWhere()) {
            assertNotNull(actual.getWhere(), assertContext.getText("Actual where segment should exist."));
            WhereClauseAssert.assertIs(assertContext, actual.getWhere().get(), expected.getWhere());
        } else {
            assertNull(actual.getWhere().orElse(null), assertContext.getText("Actual where segment should not exist."));
        }
        if (null != expected.getOrderBy()) {
            assertNotNull(actual.getOrderBy(), assertContext.getText("Actual order by segment should exist."));
            OrderByClauseAssert.assertIs(assertContext, actual.getOrderBy().get(), expected.getOrderBy());
        } else {
            assertNull(actual.getOrderBy().orElse(null), assertContext.getText("Actual order by segment should not exist."));
        }
        if (null != expected.getLimit()) {
            assertNotNull(actual.getLimit(), assertContext.getText("Actual limit segment should exist."));
            LimitClauseAssert.assertRowCount(assertContext, actual.getLimit().get().getRowCount().orElse(null), expected.getLimit().getRowCount());
        } else {
            assertNull(actual.getLimit().orElse(null), assertContext.getText("Actual limit segment should not exist."));
        }
    }
}
