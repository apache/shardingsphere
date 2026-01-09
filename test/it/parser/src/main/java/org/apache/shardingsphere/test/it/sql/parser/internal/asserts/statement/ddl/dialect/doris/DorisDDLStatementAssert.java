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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.doris;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterStoragePolicyStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisResumeJobStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.doris.type.DorisAlterStoragePolicyStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisAlterStoragePolicyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisResumeJobStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.function.CreateFunctionStatementTestCase;

/**
 * DDL statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisDDLStatementAssert {
    
    /**
     * Assert DDL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual DDL statement
     * @param expected expected DDL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DDLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof DorisResumeJobStatement) {
            DorisResumeJobStatementAssert.assertIs(assertContext, (DorisResumeJobStatement) actual, (DorisResumeJobStatementTestCase) expected);
        } else if (actual instanceof DorisCreateFunctionStatement) {
            DorisCreateFunctionStatementAssert.assertIs(assertContext, (DorisCreateFunctionStatement) actual, (CreateFunctionStatementTestCase) expected);
        } else if (actual instanceof DorisAlterStoragePolicyStatement) {
            DorisAlterStoragePolicyStatementAssert.assertIs(assertContext, (DorisAlterStoragePolicyStatement) actual, (DorisAlterStoragePolicyStatementTestCase) expected);
        }
    }
}
