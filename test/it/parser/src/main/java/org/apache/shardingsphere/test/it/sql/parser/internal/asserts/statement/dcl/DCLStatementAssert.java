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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.mysql.MySQLDCLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.postgresql.PostgreSQLDCLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.sqlserver.SQLServerDCLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.standard.StandardDCLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;

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
        StandardDCLStatementAssert.assertIs(assertContext, actual, expected);
        MySQLDCLStatementAssert.assertIs(assertContext, actual, expected);
        PostgreSQLDCLStatementAssert.assertIs(assertContext, actual, expected);
        SQLServerDCLStatementAssert.assertIs(assertContext, actual, expected);
    }
}
