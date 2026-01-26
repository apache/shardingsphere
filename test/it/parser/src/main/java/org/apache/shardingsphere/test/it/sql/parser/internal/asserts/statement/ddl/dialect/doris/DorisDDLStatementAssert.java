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
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisDropMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisPauseMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisRefreshMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisResumeMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCancelMaterializedViewTaskStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.doris.type.DorisAlterStoragePolicyStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.doris.type.DorisAlterMaterializedViewStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.doris.type.DorisCreateMaterializedViewStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisAlterStoragePolicyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisResumeJobStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisAlterMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisCreateMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisDropMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisPauseMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisRefreshMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisResumeMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisCancelMaterializedViewTaskStatementTestCase;
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
        } else if (actual instanceof DorisPauseMaterializedViewStatement) {
            DorisPauseMaterializedViewStatementAssert.assertIs(assertContext, (DorisPauseMaterializedViewStatement) actual, (DorisPauseMaterializedViewStatementTestCase) expected);
        } else if (actual instanceof DorisResumeMaterializedViewStatement) {
            DorisResumeMaterializedViewStatementAssert.assertIs(assertContext, (DorisResumeMaterializedViewStatement) actual, (DorisResumeMaterializedViewStatementTestCase) expected);
        } else if (actual instanceof DorisDropMaterializedViewStatement) {
            DorisDropMaterializedViewStatementAssert.assertIs(assertContext, (DorisDropMaterializedViewStatement) actual, (DorisDropMaterializedViewStatementTestCase) expected);
        } else if (actual instanceof DorisRefreshMaterializedViewStatement) {
            DorisRefreshMaterializedViewStatementAssert.assertIs(assertContext, (DorisRefreshMaterializedViewStatement) actual, (DorisRefreshMaterializedViewStatementTestCase) expected);
        } else if (actual instanceof DorisCreateMaterializedViewStatement) {
            DorisCreateMaterializedViewStatementAssert.assertIs(assertContext, (DorisCreateMaterializedViewStatement) actual, (DorisCreateMaterializedViewStatementTestCase) expected);
        } else if (actual instanceof DorisAlterMaterializedViewStatement) {
            DorisAlterMaterializedViewStatementAssert.assertIs(assertContext, (DorisAlterMaterializedViewStatement) actual, (DorisAlterMaterializedViewStatementTestCase) expected);
        } else if (actual instanceof DorisCancelMaterializedViewTaskStatement) {
            DorisCancelMaterializedViewTaskStatementAssert.assertIs(assertContext, (DorisCancelMaterializedViewTaskStatement) actual, (DorisCancelMaterializedViewTaskStatementTestCase) expected);
        }
    }
}
