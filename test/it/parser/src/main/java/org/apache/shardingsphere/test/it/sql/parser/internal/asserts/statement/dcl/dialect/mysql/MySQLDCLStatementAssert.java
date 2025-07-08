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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.mysql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.MySQLGrantStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.MySQLRevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.role.MySQLSetDefaultRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.user.MySQLRenameUserStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.user.MySQLSetPasswordStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.mysql.type.MySQLGrantStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.mysql.type.MySQLRenameUserStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.mysql.type.MySQLRevokeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.mysql.type.MySQLSetDefaultRoleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.mysql.type.MySQLSetPasswordStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.mysql.MySQLRenameUserStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.mysql.MySQLSetDefaultRoleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.mysql.MySQLSetPasswordStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.standard.GrantStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.standard.RevokeStatementTestCase;

/**
 * DCL statement assert for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLDCLStatementAssert {
    
    /**
     * Assert DCL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual DCL statement
     * @param expected expected DCL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DCLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof MySQLRenameUserStatement) {
            MySQLRenameUserStatementAssert.assertIs(assertContext, (MySQLRenameUserStatement) actual, (MySQLRenameUserStatementTestCase) expected);
        } else if (actual instanceof MySQLSetDefaultRoleStatement) {
            MySQLSetDefaultRoleStatementAssert.assertIs(assertContext, (MySQLSetDefaultRoleStatement) actual, (MySQLSetDefaultRoleStatementTestCase) expected);
        } else if (actual instanceof MySQLSetPasswordStatement) {
            MySQLSetPasswordStatementAssert.assertIs(assertContext, (MySQLSetPasswordStatement) actual, (MySQLSetPasswordStatementTestCase) expected);
        } else if (actual instanceof MySQLGrantStatement) {
            MySQLGrantStatementAssert.assertIs(assertContext, (MySQLGrantStatement) actual, (GrantStatementTestCase) expected);
        } else if (actual instanceof MySQLRevokeStatement) {
            MySQLRevokeStatementAssert.assertIs(assertContext, (MySQLRevokeStatement) actual, (RevokeStatementTestCase) expected);
        }
    }
}
