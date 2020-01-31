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

package org.apache.shardingsphere.sql.parser.integrate.asserts.statement.tcl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.SQLParserTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.tcl.SetAutoCommitStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.tcl.SetTransactionStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.tcl.TCLStatementTestCase;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.SetTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.TCLStatement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * TCL statement assert.
 *
 * @author zhangliang
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
        } else if (actual instanceof SetAutoCommitStatement) {
            SetAutoCommitStatementAssert.assertIs(assertContext, (SetAutoCommitStatement) actual, (SetAutoCommitStatementTestCase) expected);
        } else {
            assertThat(actual.getClass().getName(), is(((TCLStatementTestCase) expected).getTclActualStatementClassType()));
        }
    }
}
