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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.standard;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.EmptyStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowBuildIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisAlterSqlBlockRuleStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisDropSqlBlockRuleStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowSqlBlockRuleStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowRoutineLoadTaskStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowRoutineLoadStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.standard.type.EmptyStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.standard.type.ExplainStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.standard.type.SetParameterStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.standard.type.ShowStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.standard.type.ShowBuildIndexStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisAlterSqlBlockRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisDropSqlBlockRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowSqlBlockRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowRoutineLoadTaskStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowRoutineLoadStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisAlterSqlBlockRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisDropSqlBlockRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowSqlBlockRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowRoutineLoadTaskStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowRoutineLoadStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowBuildIndexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.standard.EmptyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.standard.ExplainStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.standard.SetParameterStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.standard.ShowStatementTestCase;

/**
 * Standard DAL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StandardDALStatementAssert {
    
    /**
     * Assert DAL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual DAL statement
     * @param expected expected DAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DALStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof EmptyStatement) {
            EmptyStatementAssert.assertIs(assertContext, (EmptyStatement) actual, (EmptyStatementTestCase) expected);
        } else if (actual instanceof ExplainStatement) {
            ExplainStatementAssert.assertIs(assertContext, (ExplainStatement) actual, (ExplainStatementTestCase) expected);
        } else if (actual instanceof SetStatement) {
            SetParameterStatementAssert.assertIs(assertContext, (SetStatement) actual, (SetParameterStatementTestCase) expected);
        } else if (actual instanceof ShowStatement) {
            ShowStatementAssert.assertIs(assertContext, (ShowStatement) actual, (ShowStatementTestCase) expected);
        } else if (actual instanceof ShowBuildIndexStatement) {
            ShowBuildIndexStatementAssert.assertIs(assertContext, (ShowBuildIndexStatement) actual, (ShowBuildIndexStatementTestCase) expected);
        } else if (actual instanceof DorisAlterSqlBlockRuleStatement) {
            DorisAlterSqlBlockRuleStatementAssert.assertIs(assertContext, (DorisAlterSqlBlockRuleStatement) actual, (DorisAlterSqlBlockRuleStatementTestCase) expected);
        } else if (actual instanceof DorisDropSqlBlockRuleStatement) {
            DorisDropSqlBlockRuleStatementAssert.assertIs(assertContext, (DorisDropSqlBlockRuleStatement) actual, (DorisDropSqlBlockRuleStatementTestCase) expected);
        } else if (actual instanceof DorisShowSqlBlockRuleStatement) {
            DorisShowSqlBlockRuleStatementAssert.assertIs(assertContext, (DorisShowSqlBlockRuleStatement) actual, (DorisShowSqlBlockRuleStatementTestCase) expected);
        } else if (actual instanceof DorisShowRoutineLoadTaskStatement) {
            DorisShowRoutineLoadTaskStatementAssert.assertIs(assertContext, (DorisShowRoutineLoadTaskStatement) actual, (DorisShowRoutineLoadTaskStatementTestCase) expected);
        } else if (actual instanceof DorisShowRoutineLoadStatement) {
            DorisShowRoutineLoadStatementAssert.assertIs(assertContext, (DorisShowRoutineLoadStatement) actual, (DorisShowRoutineLoadStatementTestCase) expected);
        }
    }
}
