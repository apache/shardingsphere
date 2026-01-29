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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.doris;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCancelMaterializedViewTaskStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisCancelMaterializedViewTaskStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Cancel materialized view task statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisCancelMaterializedViewTaskStatementAssert {
    
    /**
     * Assert cancel materialized view task statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual cancel materialized view task statement
     * @param expected expected cancel materialized view task statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisCancelMaterializedViewTaskStatement actual, final DorisCancelMaterializedViewTaskStatementTestCase expected) {
        assertTaskId(assertContext, actual, expected);
        assertMaterializedViewName(assertContext, actual, expected);
    }
    
    private static void assertTaskId(final SQLCaseAssertContext assertContext, final DorisCancelMaterializedViewTaskStatement actual, final DorisCancelMaterializedViewTaskStatementTestCase expected) {
        if (null == expected.getTaskId()) {
            assertNull(actual.getTaskId(), assertContext.getText("Actual task ID should not exist."));
        } else {
            assertNotNull(actual.getTaskId(), assertContext.getText("Actual task ID should exist."));
            assertThat(assertContext.getText("Task ID assertion error: "), actual.getTaskId(), is(expected.getTaskId()));
        }
    }
    
    private static void assertMaterializedViewName(final SQLCaseAssertContext assertContext, final DorisCancelMaterializedViewTaskStatement actual,
                                                   final DorisCancelMaterializedViewTaskStatementTestCase expected) {
        if (null == expected.getMaterializedViewName()) {
            assertNull(actual.getMaterializedViewName(), assertContext.getText("Actual materialized view name should not exist."));
        } else {
            assertNotNull(actual.getMaterializedViewName(), assertContext.getText("Actual materialized view name should exist."));
            assertThat(assertContext.getText("Materialized view name assertion error: "), actual.getMaterializedViewName(), is(expected.getMaterializedViewName()));
        }
    }
}
