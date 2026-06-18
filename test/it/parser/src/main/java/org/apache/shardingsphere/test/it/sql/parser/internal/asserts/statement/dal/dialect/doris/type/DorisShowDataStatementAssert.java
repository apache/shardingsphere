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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowDataStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.orderby.OrderByClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowDataStatementTestCase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Show data statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisShowDataStatementAssert {
    
    /**
     * Assert show data statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show data statement
     * @param expected expected show data statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisShowDataStatement actual, final DorisShowDataStatementTestCase expected) {
        if (null != expected.getTable()) {
            assertNotNull(actual.getTable().orElse(null), assertContext.getText("Actual table should exist."));
            TableAssert.assertIs(assertContext, actual.getTable().get(), expected.getTable());
        } else {
            assertNull(actual.getTable().orElse(null), assertContext.getText("Actual table should not exist."));
        }
        if (null != expected.getOrderBy()) {
            assertNotNull(actual.getOrderBy().orElse(null), assertContext.getText("Actual order by segment should exist."));
            OrderByClauseAssert.assertIs(assertContext, actual.getOrderBy().get(), expected.getOrderBy());
        } else {
            assertNull(actual.getOrderBy().orElse(null), assertContext.getText("Actual order by segment should not exist."));
        }
    }
}
