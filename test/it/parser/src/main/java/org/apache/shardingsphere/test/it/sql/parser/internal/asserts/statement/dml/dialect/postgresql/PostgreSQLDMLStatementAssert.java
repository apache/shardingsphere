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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.dialect.postgresql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dml.PostgreSQLCopyStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.returning.ReturningClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.with.WithClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.dialect.postgresql.type.PostgreSQLCopyStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.insert.ExpectedReturningClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.postgresql.PostgreSQLCopyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.DeleteStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.UpdateStatementTestCase;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DML statement assert for PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLDMLStatementAssert {
    
    /**
     * Assert DML statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual DML statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DMLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof PostgreSQLCopyStatement) {
            PostgreSQLCopyStatementAssert.assertIs(assertContext, (PostgreSQLCopyStatement) actual, (PostgreSQLCopyStatementTestCase) expected);
        }
        if (!"PostgreSQL".equals(actual.getDatabaseType().getType())) {
            return;
        }
        if (actual instanceof DeleteStatement) {
            assertReturningClause(assertContext, ((DeleteStatement) actual).getReturning().orElse(null), ((DeleteStatementTestCase) expected).getReturningClause());
        }
        if (actual instanceof UpdateStatement) {
            UpdateStatement actualUpdateStatement = (UpdateStatement) actual;
            UpdateStatementTestCase expectedUpdateStatement = (UpdateStatementTestCase) expected;
            assertWithClause(assertContext, actualUpdateStatement, expectedUpdateStatement);
            assertReturningClause(assertContext, actualUpdateStatement.getReturning().orElse(null), expectedUpdateStatement.getReturningClause());
        }
    }
    
    /**
     * Assert the presence and contents of a RETURNING clause.
     *
     * @param assertContext assert context
     * @param actual returning segment, or null if absent
     * @param expected returning clause, or null if absent
     */
    private static void assertReturningClause(final SQLCaseAssertContext assertContext, final ReturningSegment actual, final ExpectedReturningClause expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual returning segment should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual returning segment should exist."));
            ReturningClauseAssert.assertIs(assertContext, actual, expected);
        }
    }
    
    private static void assertWithClause(final SQLCaseAssertContext assertContext, final UpdateStatement actual, final UpdateStatementTestCase expected) {
        if (null == expected.getWithClause()) {
            assertFalse(actual.getWith().isPresent(), assertContext.getText("Actual with segment should not exist."));
        } else {
            assertTrue(actual.getWith().isPresent(), assertContext.getText("Actual with segment should exist."));
            WithClauseAssert.assertIs(assertContext, actual.getWith().get(), expected.getWithClause());
        }
    }
}
