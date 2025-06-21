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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.SQLServerAlterLoginStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.AlterRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.AlterUserStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.SQLServerCreateLoginStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.CreateRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.CreateUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DenyUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.SQLServerDropLoginStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DropRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DropUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dcl.PostgreSQLReassignOwnedStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.RenameUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.RevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.SetDefaultRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.SetPasswordStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.SetRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.SQLServerSetUserStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.AlterLoginStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.AlterRoleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.AlterUserStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.SQLServerCreateLoginStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.CreateRoleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.CreateUserStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.DenyUserStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.DropLoginStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.DropRoleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.DropUserStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.GrantStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.PostgreSQLReassignOwnedStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.RenameUserStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.RevokeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.SetDefaultRoleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.SetPasswordStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.SetRoleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.impl.sqlserver.SQLServerSetUserStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.AlterLoginStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.AlterRoleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.AlterUserStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.CreateLoginStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.CreateRoleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.CreateUserStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.DenyUserStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.DropLoginStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.DropRoleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.DropUserStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.GrantStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.ReassignOwnedStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.RenameUserStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.RevokeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.SetDefaultRoleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.SetPasswordStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.SetRoleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.SetUserStatementTestCase;

/**
 * DCL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DCLStatementAssert {
    
    /**
     * Assert DCL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual DCL statement
     * @param expected expected DCL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DCLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof GrantStatement) {
            GrantStatementAssert.assertIs(assertContext, (GrantStatement) actual, (GrantStatementTestCase) expected);
        } else if (actual instanceof RevokeStatement) {
            RevokeStatementAssert.assertIs(assertContext, (RevokeStatement) actual, (RevokeStatementTestCase) expected);
        } else if (actual instanceof CreateUserStatement) {
            CreateUserStatementAssert.assertIs(assertContext, (CreateUserStatement) actual, (CreateUserStatementTestCase) expected);
        } else if (actual instanceof AlterUserStatement) {
            AlterUserStatementAssert.assertIs(assertContext, (AlterUserStatement) actual, (AlterUserStatementTestCase) expected);
        } else if (actual instanceof DropUserStatement) {
            DropUserStatementAssert.assertIs(assertContext, (DropUserStatement) actual, (DropUserStatementTestCase) expected);
        } else if (actual instanceof RenameUserStatement) {
            RenameUserStatementAssert.assertIs(assertContext, (RenameUserStatement) actual, (RenameUserStatementTestCase) expected);
        } else if (actual instanceof DenyUserStatement) {
            DenyUserStatementAssert.assertIs(assertContext, (DenyUserStatement) actual, (DenyUserStatementTestCase) expected);
        } else if (actual instanceof SQLServerCreateLoginStatement) {
            SQLServerCreateLoginStatementAssert.assertIs(assertContext, (SQLServerCreateLoginStatement) actual, (CreateLoginStatementTestCase) expected);
        } else if (actual instanceof SQLServerAlterLoginStatement) {
            AlterLoginStatementAssert.assertIs(assertContext, (SQLServerAlterLoginStatement) actual, (AlterLoginStatementTestCase) expected);
        } else if (actual instanceof SQLServerDropLoginStatement) {
            DropLoginStatementAssert.assertIs(assertContext, (SQLServerDropLoginStatement) actual, (DropLoginStatementTestCase) expected);
        } else if (actual instanceof CreateRoleStatement) {
            CreateRoleStatementAssert.assertIs(assertContext, (CreateRoleStatement) actual, (CreateRoleStatementTestCase) expected);
        } else if (actual instanceof AlterRoleStatement) {
            AlterRoleStatementAssert.assertIs(assertContext, (AlterRoleStatement) actual, (AlterRoleStatementTestCase) expected);
        } else if (actual instanceof DropRoleStatement) {
            DropRoleStatementAssert.assertIs(assertContext, (DropRoleStatement) actual, (DropRoleStatementTestCase) expected);
        } else if (actual instanceof SetRoleStatement) {
            SetRoleStatementAssert.assertIs(assertContext, (SetRoleStatement) actual, (SetRoleStatementTestCase) expected);
        } else if (actual instanceof SetDefaultRoleStatement) {
            SetDefaultRoleStatementAssert.assertIs(assertContext, (SetDefaultRoleStatement) actual, (SetDefaultRoleStatementTestCase) expected);
        } else if (actual instanceof SetPasswordStatement) {
            SetPasswordStatementAssert.assertIs(assertContext, (SetPasswordStatement) actual, (SetPasswordStatementTestCase) expected);
        } else if (actual instanceof SQLServerSetUserStatement) {
            SQLServerSetUserStatementAssert.assertIs(assertContext, (SQLServerSetUserStatement) actual, (SetUserStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLReassignOwnedStatement) {
            PostgreSQLReassignOwnedStatementAssert.assertIs(assertContext, (PostgreSQLReassignOwnedStatement) actual, (ReassignOwnedStatementTestCase) expected);
        }
    }
}
