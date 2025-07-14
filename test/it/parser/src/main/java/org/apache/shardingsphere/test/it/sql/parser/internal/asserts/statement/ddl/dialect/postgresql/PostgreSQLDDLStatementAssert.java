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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLClusterStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDeclareStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLListenStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLNotifyStmtStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLOpenStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLSecurityLabelStmtStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLUnlistenStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.index.PostgreSQLReindexStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql.type.PostgreSQLClusterStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql.type.PostgreSQLDeclareStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql.type.PostgreSQLListenStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql.type.PostgreSQLNotifyStmtStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql.type.PostgreSQLOpenStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql.type.PostgreSQLReindexStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql.type.PostgreSQLSecurityLabelStmtStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql.type.PostgreSQLUnlistenStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLClusterStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLDeclareStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLListenStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLNotifyStmtStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLOpenStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLReindexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLSecurityLabelStmtStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLUnlistenStatementTestCase;

/**
 * DDL statement assert for PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLDDLStatementAssert {
    
    /**
     * Assert DDL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual DDL statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DDLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof PostgreSQLDeclareStatement) {
            PostgreSQLDeclareStatementAssert.assertIs(assertContext, (PostgreSQLDeclareStatement) actual, (PostgreSQLDeclareStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLClusterStatement) {
            PostgreSQLClusterStatementAssert.assertIs(assertContext, (PostgreSQLClusterStatement) actual, (PostgreSQLClusterStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLListenStatement) {
            PostgreSQLListenStatementAssert.assertIs(assertContext, (PostgreSQLListenStatement) actual, (PostgreSQLListenStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLUnlistenStatement) {
            PostgreSQLUnlistenStatementAssert.assertIs(assertContext, (PostgreSQLUnlistenStatement) actual, (PostgreSQLUnlistenStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLNotifyStmtStatement) {
            PostgreSQLNotifyStmtStatementAssert.assertIs(assertContext, (PostgreSQLNotifyStmtStatement) actual, (PostgreSQLNotifyStmtStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLReindexStatement) {
            PostgreSQLReindexStatementAssert.assertIs(assertContext, (PostgreSQLReindexStatement) actual, (PostgreSQLReindexStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLSecurityLabelStmtStatement) {
            PostgreSQLSecurityLabelStmtStatementAssert.assertIs(assertContext, (PostgreSQLSecurityLabelStmtStatement) actual, (PostgreSQLSecurityLabelStmtStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLOpenStatement) {
            PostgreSQLOpenStatementAssert.assertIs(assertContext, (PostgreSQLOpenStatement) actual, (PostgreSQLOpenStatementTestCase) expected);
        }
    }
}
