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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.sqlserver;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.SQLServerGrantStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.SQLServerRevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.login.SQLServerAlterLoginStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.login.SQLServerCreateLoginStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.login.SQLServerDropLoginStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.user.SQLServerDenyUserStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.user.SQLServerSetUserStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.sqlserver.type.SQLServerAlterLoginStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.sqlserver.type.SQLServerCreateLoginStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.sqlserver.type.SQLServerDenyUserStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.sqlserver.type.SQLServerDropLoginStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.sqlserver.type.SQLServerGrantStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.sqlserver.type.SQLServerRevokeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.sqlserver.type.SQLServerSetUserStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.sqlserver.SQLServerAlterLoginStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.sqlserver.SQLServerCreateLoginStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.sqlserver.SQLServerDenyUserStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.sqlserver.SQLServerDropLoginStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.sqlserver.SQLServerSetUserStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.standard.GrantStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.standard.RevokeStatementTestCase;

/**
 * DCL statement assert for SQLServer.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLServerDCLStatementAssert {
    
    /**
     * Assert DCL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual DCL statement
     * @param expected expected DCL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DCLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof SQLServerDenyUserStatement) {
            SQLServerDenyUserStatementAssert.assertIs(assertContext, (SQLServerDenyUserStatement) actual, (SQLServerDenyUserStatementTestCase) expected);
        } else if (actual instanceof SQLServerCreateLoginStatement) {
            SQLServerCreateLoginStatementAssert.assertIs(assertContext, (SQLServerCreateLoginStatement) actual, (SQLServerCreateLoginStatementTestCase) expected);
        } else if (actual instanceof SQLServerAlterLoginStatement) {
            SQLServerAlterLoginStatementAssert.assertIs(assertContext, (SQLServerAlterLoginStatement) actual, (SQLServerAlterLoginStatementTestCase) expected);
        } else if (actual instanceof SQLServerDropLoginStatement) {
            SQLServerDropLoginStatementAssert.assertIs(assertContext, (SQLServerDropLoginStatement) actual, (SQLServerDropLoginStatementTestCase) expected);
        } else if (actual instanceof SQLServerSetUserStatement) {
            SQLServerSetUserStatementAssert.assertIs(assertContext, (SQLServerSetUserStatement) actual, (SQLServerSetUserStatementTestCase) expected);
        } else if (actual instanceof SQLServerGrantStatement) {
            SQLServerGrantStatementAssert.assertIs(assertContext, (SQLServerGrantStatement) actual, (GrantStatementTestCase) expected);
        } else if (actual instanceof SQLServerRevokeStatement) {
            SQLServerRevokeStatementAssert.assertIs(assertContext, (SQLServerRevokeStatement) actual, (RevokeStatementTestCase) expected);
        }
    }
}
