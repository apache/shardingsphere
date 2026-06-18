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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.SQLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.standard.ExplainStatementTestCase;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Explain statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExplainStatementAssert {
    
    /**
     * Assert explain statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual explain statement
     * @param expected expected explain statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ExplainStatement actual, final ExplainStatementTestCase expected) {
        if (null != expected.getSelectClause()) {
            assertNotNull(actual.getExplainableSQLStatement(), assertContext.getText("Actual statement should exist."));
            SQLStatementAssert.assertIs(assertContext, actual.getExplainableSQLStatement(), expected.getSelectClause());
        } else if (null != expected.getUpdateClause()) {
            assertNotNull(actual.getExplainableSQLStatement(), assertContext.getText("Actual statement should exist."));
            SQLStatementAssert.assertIs(assertContext, actual.getExplainableSQLStatement(), expected.getUpdateClause());
        } else if (null != expected.getInsertClause()) {
            assertNotNull(actual.getExplainableSQLStatement(), assertContext.getText("Actual statement should exist."));
            SQLStatementAssert.assertIs(assertContext, actual.getExplainableSQLStatement(), expected.getInsertClause());
        } else if (null != expected.getDeleteClause()) {
            assertNotNull(actual.getExplainableSQLStatement(), assertContext.getText("Actual statement should exist."));
            SQLStatementAssert.assertIs(assertContext, actual.getExplainableSQLStatement(), expected.getDeleteClause());
        } else if (null != expected.getCreateTableAsSelectClause()) {
            assertNotNull(actual.getExplainableSQLStatement(), assertContext.getText("Actual statement should exist."));
            SQLStatementAssert.assertIs(assertContext, actual.getExplainableSQLStatement(), expected.getCreateTableAsSelectClause());
        }
    }
}
