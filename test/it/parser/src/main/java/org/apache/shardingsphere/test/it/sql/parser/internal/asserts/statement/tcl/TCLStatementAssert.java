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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.tcl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetConstraintsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.tcl.type.BeginTransactionStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.tcl.type.CommitStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.tcl.type.RollbackStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.tcl.type.SavepointStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.tcl.type.SetAutoCommitStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.tcl.type.SetConstraintsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.tcl.type.SetTransactionStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.BeginTransactionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.CommitStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.RollbackStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.SavepointStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.SetAutoCommitStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.SetConstraintsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.SetTransactionStatementTestCase;

/**
 * TCL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TCLStatementAssert {
    
    /**
     * Assert TCL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual TCL statement
     * @param expected expected TCL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final TCLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof SetTransactionStatement) {
            SetTransactionStatementAssert.assertIs(assertContext, (SetTransactionStatement) actual, (SetTransactionStatementTestCase) expected);
        } else if (actual instanceof BeginTransactionStatement) {
            BeginTransactionStatementAssert.assertIs(assertContext, (BeginTransactionStatement) actual, (BeginTransactionStatementTestCase) expected);
        } else if (actual instanceof SetAutoCommitStatement) {
            SetAutoCommitStatementAssert.assertIs(assertContext, (SetAutoCommitStatement) actual, (SetAutoCommitStatementTestCase) expected);
        } else if (actual instanceof CommitStatement) {
            CommitStatementAssert.assertIs(assertContext, (CommitStatement) actual, (CommitStatementTestCase) expected);
        } else if (actual instanceof RollbackStatement) {
            RollbackStatementAssert.assertIs(assertContext, (RollbackStatement) actual, (RollbackStatementTestCase) expected);
        } else if (actual instanceof SavepointStatement) {
            SavepointStatementAssert.assertIs(assertContext, (SavepointStatement) actual, (SavepointStatementTestCase) expected);
        } else if (actual instanceof SetConstraintsStatement) {
            SetConstraintsStatementAssert.assertIs(assertContext, (SetConstraintsStatement) actual, (SetConstraintsStatementTestCase) expected);
        }
    }
}
