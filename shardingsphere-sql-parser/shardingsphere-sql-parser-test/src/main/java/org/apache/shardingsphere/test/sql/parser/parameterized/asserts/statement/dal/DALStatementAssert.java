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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dal;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dal.impl.DescribeStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dal.impl.SetVariableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dal.impl.ShowColumnsStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dal.impl.ShowCreateTableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dal.impl.ShowDatabasesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dal.impl.ShowIndexStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dal.impl.ShowStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dal.impl.ShowTableStatusStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dal.impl.ShowTablesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dal.impl.MySQLUseStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.DescribeStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.SetVariableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowColumnsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowCreateTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowDatabasesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowIndexStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowTableStatusStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowTablesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.UseStatementTestCase;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLDescribeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLShowStatement;

/**
 * DAL statement assert.
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DALStatementAssert {
    
    /**
     * Assert DAL statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual DAL statement
     * @param expected expected DAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DALStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof MySQLUseStatement) {
            MySQLUseStatementAssert.assertIs(assertContext, (MySQLUseStatement) actual, (UseStatementTestCase) expected);
        } else if (actual instanceof MySQLDescribeStatement) {
            DescribeStatementAssert.assertIs(assertContext, (MySQLDescribeStatement) actual, (DescribeStatementTestCase) expected);
        } else if (actual instanceof MySQLShowDatabasesStatement) {
            ShowDatabasesStatementAssert.assertIs(assertContext, (MySQLShowDatabasesStatement) actual, (ShowDatabasesStatementTestCase) expected);
        } else if (actual instanceof MySQLShowTablesStatement) {
            ShowTablesStatementAssert.assertIs(assertContext, (MySQLShowTablesStatement) actual, (ShowTablesStatementTestCase) expected);
        } else if (actual instanceof MySQLShowColumnsStatement) {
            ShowColumnsStatementAssert.assertIs(assertContext, (MySQLShowColumnsStatement) actual, (ShowColumnsStatementTestCase) expected);
        } else if (actual instanceof MySQLShowCreateTableStatement) {
            ShowCreateTableStatementAssert.assertIs(assertContext, (MySQLShowCreateTableStatement) actual, (ShowCreateTableStatementTestCase) expected);
        } else if (actual instanceof MySQLShowTableStatusStatement) {
            ShowTableStatusStatementAssert.assertIs(assertContext, (MySQLShowTableStatusStatement) actual, (ShowTableStatusStatementTestCase) expected);
        } else if (actual instanceof MySQLShowIndexStatement) {
            ShowIndexStatementAssert.assertIs(assertContext, (MySQLShowIndexStatement) actual, (ShowIndexStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLShowStatement) {
            ShowStatementAssert.assertIs(assertContext, (PostgreSQLShowStatement) actual, (ShowStatementTestCase) expected);
        } else if (actual instanceof SetStatement) {
            SetVariableStatementAssert.assertIs(assertContext, (SetStatement) actual, (SetVariableStatementTestCase) expected);
        }
    }
}
