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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.oracle;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.OracleAlterSessionStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.OracleAlterSystemStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.OracleAnalyzeStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.OracleAuditStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.OracleNoAuditStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.OraclePurgeStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.flashback.OracleFlashbackTableStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.statistics.OracleAssociateStatisticsStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.statistics.OracleDisassociateStatisticsStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.oracle.type.OracleAlterSessionStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.oracle.type.OracleAlterSystemStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.oracle.type.OracleAnalyzeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.oracle.type.OracleAssociateStatisticsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.oracle.type.OracleAuditStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.oracle.type.OracleDisassociateStatisticsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.oracle.type.OracleFlashbackTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.oracle.type.OracleNoAuditStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.oracle.type.OraclePurgeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleAlterSessionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleAlterSystemStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleAnalyzeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleAuditStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleNoAuditStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OraclePurgeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.flashback.OracleFlashbackTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.statistics.OracleAssociateStatisticsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.statistics.OracleDisassociateStatisticsStatementTestCase;

/**
 * DDL statement assert for Oracle.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OracleDDLStatementAssert {
    
    /**
     * Assert DDL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual DDL statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DDLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof OracleAlterSessionStatement) {
            OracleAlterSessionStatementAssert.assertIs(assertContext, (OracleAlterSessionStatement) actual, (OracleAlterSessionStatementTestCase) expected);
        } else if (actual instanceof OracleAlterSystemStatement) {
            OracleAlterSystemStatementAssert.assertIs(assertContext, (OracleAlterSystemStatement) actual, (OracleAlterSystemStatementTestCase) expected);
        } else if (actual instanceof OracleAnalyzeStatement) {
            OracleAnalyzeStatementAssert.assertIs(assertContext, (OracleAnalyzeStatement) actual, (OracleAnalyzeStatementTestCase) expected);
        } else if (actual instanceof OracleAssociateStatisticsStatement) {
            OracleAssociateStatisticsStatementAssert.assertIs(assertContext, (OracleAssociateStatisticsStatement) actual, (OracleAssociateStatisticsStatementTestCase) expected);
        } else if (actual instanceof OracleDisassociateStatisticsStatement) {
            OracleDisassociateStatisticsStatementAssert.assertIs(assertContext, (OracleDisassociateStatisticsStatement) actual, (OracleDisassociateStatisticsStatementTestCase) expected);
        } else if (actual instanceof OracleAuditStatement) {
            OracleAuditStatementAssert.assertIs(assertContext, (OracleAuditStatement) actual, (OracleAuditStatementTestCase) expected);
        } else if (actual instanceof OracleNoAuditStatement) {
            OracleNoAuditStatementAssert.assertIs(assertContext, (OracleNoAuditStatement) actual, (OracleNoAuditStatementTestCase) expected);
        } else if (actual instanceof OracleFlashbackTableStatement) {
            OracleFlashbackTableStatementAssert.assertIs(assertContext, (OracleFlashbackTableStatement) actual, (OracleFlashbackTableStatementTestCase) expected);
        } else if (actual instanceof OraclePurgeStatement) {
            OraclePurgeStatementAssert.assertIs(assertContext, (OraclePurgeStatement) actual, (OraclePurgeStatementTestCase) expected);
        }
    }
}
