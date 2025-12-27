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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisAlterResourceStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisAlterSystemStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisCreateSqlBlockRuleStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisAlterResourceStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisAlterSystemStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisCreateSqlBlockRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisAlterResourceStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisAlterSystemStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisCreateSqlBlockRuleStatementTestCase;

/**
 * DAL statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisDALStatementAssert {
    
    /**
     * Assert DAL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual DAL statement
     * @param expected expected DAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DALStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof DorisAlterResourceStatement) {
            DorisAlterResourceStatementAssert.assertIs(assertContext, (DorisAlterResourceStatement) actual, (DorisAlterResourceStatementTestCase) expected);
        } else if (actual instanceof DorisAlterSystemStatement) {
            DorisAlterSystemStatementAssert.assertIs(assertContext, (DorisAlterSystemStatement) actual, (DorisAlterSystemStatementTestCase) expected);
        } else if (actual instanceof DorisCreateSqlBlockRuleStatement) {
            DorisCreateSqlBlockRuleStatementAssert.assertIs(assertContext, (DorisCreateSqlBlockRuleStatement) actual, (DorisCreateSqlBlockRuleStatementTestCase) expected);
        }
    }
}
