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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rul.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.rul.SQLRULStatement;
import org.apache.shardingsphere.distsql.parser.statement.rul.sql.FormatStatement;
import org.apache.shardingsphere.distsql.parser.statement.rul.sql.ParseStatement;
import org.apache.shardingsphere.distsql.parser.statement.rul.sql.PreviewStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rul.impl.sql.FormatSQLStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rul.impl.sql.ParseStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rul.impl.sql.PreviewStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rul.FormatSQLStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rul.ParseStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rul.PreviewStatementTestCase;

/**
 * SQL RUL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLRULStatementAssert {
    
    /**
     * Assert SQL RUL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual SQL RUL statement
     * @param expected expected SQL RUL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SQLRULStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof PreviewStatement) {
            PreviewStatementAssert.assertIs(assertContext, (PreviewStatement) actual, (PreviewStatementTestCase) expected);
        } else if (actual instanceof ParseStatement) {
            ParseStatementAssert.assertIs(assertContext, (ParseStatement) actual, (ParseStatementTestCase) expected);
        } else if (actual instanceof FormatStatement) {
            FormatSQLStatementAssert.assertIs(assertContext, (FormatStatement) actual, (FormatSQLStatementTestCase) expected);
        }
    }
}
