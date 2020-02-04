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

package org.apache.shardingsphere.sql.parser.integrate.asserts.statement.dcl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.sql.parser.integrate.asserts.statement.dcl.impl.CreateUserStatementAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.statement.dcl.impl.GrantStatementAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.statement.dcl.impl.RevokeStatementAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.SQLParserTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.dcl.CreateUserStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.dcl.GrantStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.dcl.RevokeStatementTestCase;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.CreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.RevokeStatement;

/**
 * DCL statement assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DCLStatementAssert {
    
    /**
     * Assert DAL statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual DAL statement
     * @param expected expected DAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DCLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof GrantStatement) {
            GrantStatementAssert.assertIs(assertContext, (GrantStatement) actual, (GrantStatementTestCase) expected);
        } else if (actual instanceof RevokeStatement) {
            RevokeStatementAssert.assertIs(assertContext, (RevokeStatement) actual, (RevokeStatementTestCase) expected);
        } else if (actual instanceof CreateUserStatement) {
            CreateUserStatementAssert.assertIs(assertContext, (CreateUserStatement) actual, (CreateUserStatementTestCase) expected);
        }
    }
}
