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

package org.apache.shardingsphere.test.sql.parser.integrate.asserts.statement.dal;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.statement.dal.impl.DescribeStatementAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.statement.dal.impl.SetVariableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.statement.dal.impl.ShowColumnsStatementAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.statement.dal.impl.ShowCreateTableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.statement.dal.impl.ShowDatabasesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.statement.dal.impl.ShowIndexStatementAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.statement.dal.impl.ShowStatementAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.statement.dal.impl.ShowTableStatusStatementAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.statement.dal.impl.ShowTablesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.statement.dal.impl.UseStatementAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.statement.dal.DescribeStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.statement.dal.SetVariableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.statement.dal.ShowColumnsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.statement.dal.ShowCreateTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.statement.dal.ShowDatabasesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.statement.dal.ShowIndexStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.statement.dal.ShowStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.statement.dal.ShowTableStatusStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.statement.dal.ShowTablesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.statement.dal.UseStatementTestCase;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.DescribeStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowTablesStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.UseStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.postgresql.ShowStatement;

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
        if (actual instanceof UseStatement) {
            UseStatementAssert.assertIs(assertContext, (UseStatement) actual, (UseStatementTestCase) expected);
        } else if (actual instanceof DescribeStatement) {
            DescribeStatementAssert.assertIs(assertContext, (DescribeStatement) actual, (DescribeStatementTestCase) expected);
        } else if (actual instanceof ShowDatabasesStatement) {
            ShowDatabasesStatementAssert.assertIs(assertContext, (ShowDatabasesStatement) actual, (ShowDatabasesStatementTestCase) expected);
        } else if (actual instanceof ShowTablesStatement) {
            ShowTablesStatementAssert.assertIs(assertContext, (ShowTablesStatement) actual, (ShowTablesStatementTestCase) expected);
        } else if (actual instanceof ShowColumnsStatement) {
            ShowColumnsStatementAssert.assertIs(assertContext, (ShowColumnsStatement) actual, (ShowColumnsStatementTestCase) expected);
        } else if (actual instanceof ShowCreateTableStatement) {
            ShowCreateTableStatementAssert.assertIs(assertContext, (ShowCreateTableStatement) actual, (ShowCreateTableStatementTestCase) expected);
        } else if (actual instanceof ShowTableStatusStatement) {
            ShowTableStatusStatementAssert.assertIs(assertContext, (ShowTableStatusStatement) actual, (ShowTableStatusStatementTestCase) expected);
        } else if (actual instanceof ShowIndexStatement) {
            ShowIndexStatementAssert.assertIs(assertContext, (ShowIndexStatement) actual, (ShowIndexStatementTestCase) expected);
        } else if (actual instanceof ShowStatement) {
            ShowStatementAssert.assertIs(assertContext, (ShowStatement) actual, (ShowStatementTestCase) expected);
        } else if (actual instanceof SetStatement) {
            SetVariableStatementAssert.assertIs(assertContext, (SetStatement) actual, (SetVariableStatementTestCase) expected);
        }
    }
}
