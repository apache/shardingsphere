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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterSystemStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.ClusterStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CommentStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DeclareStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.ListenStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.MoveStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.NotifyStmtStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.ReindexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.RefreshMatViewStmtStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.SecurityLabelStmtStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.UnlistenStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCursorStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterSessionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterSynonymStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAnalyzeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAssociateStatisticsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAuditStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDisassociateStatisticsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleNoAuditStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.AlterIndexStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.AlterSessionStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.AlterSynonymStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.AlterSystemStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.AlterTableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.AnalyzeStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.AssociateStatisticsStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.AuditStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.CloseStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.ClusterStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.CommentStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.CreateIndexStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.CreateTableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.CursorStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.DeclareStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.DisassociateStatisticsStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.DropIndexStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.DropTableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.FetchStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.MoveStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.NoAuditStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.NotifyStmtStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.RefreshMatViewStmtStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.ReindexStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.RenameTableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.SecurityLabelStmtStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.TruncateStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.ListenStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.UnlistenStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterIndexStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterSessionStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterSynonymStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterSystemStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AnalyzeStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AssociateStatisticsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AuditStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CloseStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.ClusterStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CommentStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CreateIndexStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CreateTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CursorStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DeclareStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DisassociateStatisticsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DropIndexStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DropTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.FetchStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.MoveStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.NoAuditStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.NotifyStmtStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.RefreshMatViewStmtStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.ReindexStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.RenameTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.SecurityLabelStmtStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.TruncateStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.ListenStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.UnlistenStatementTestCase;

/**
 * DDL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DDLStatementAssert {
    
    /**
     * Assert DDL statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual DDL statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DDLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof CreateTableStatement) {
            CreateTableStatementAssert.assertIs(assertContext, (CreateTableStatement) actual, (CreateTableStatementTestCase) expected);
        } else if (actual instanceof AlterTableStatement) {
            AlterTableStatementAssert.assertIs(assertContext, (AlterTableStatement) actual, (AlterTableStatementTestCase) expected);
        } else if (actual instanceof RenameTableStatement) {
            RenameTableStatementAssert.assertIs(assertContext, (RenameTableStatement) actual, (RenameTableStatementTestCase) expected);
        } else if (actual instanceof DropTableStatement) {
            DropTableStatementAssert.assertIs(assertContext, (DropTableStatement) actual, (DropTableStatementTestCase) expected);
        } else if (actual instanceof TruncateStatement) {
            TruncateStatementAssert.assertIs(assertContext, (TruncateStatement) actual, (TruncateStatementTestCase) expected);
        } else if (actual instanceof CreateIndexStatement) {
            CreateIndexStatementAssert.assertIs(assertContext, (CreateIndexStatement) actual, (CreateIndexStatementTestCase) expected);
        } else if (actual instanceof AlterIndexStatement) {
            AlterIndexStatementAssert.assertIs(assertContext, (AlterIndexStatement) actual, (AlterIndexStatementTestCase) expected);
        } else if (actual instanceof DropIndexStatement) {
            DropIndexStatementAssert.assertIs(assertContext, (DropIndexStatement) actual, (DropIndexStatementTestCase) expected);
        } else if (actual instanceof OracleAlterSynonymStatement) {
            AlterSynonymStatementAssert.assertIs(assertContext, (OracleAlterSynonymStatement) actual, (AlterSynonymStatementTestCase) expected);
        } else if (actual instanceof OracleAlterSessionStatement) {
            AlterSessionStatementAssert.assertIs(assertContext, (OracleAlterSessionStatement) actual, (AlterSessionStatementTestCase) expected);
        } else if (actual instanceof AlterSystemStatement) {
            AlterSystemStatementAssert.assertIs(assertContext, (AlterSystemStatement) actual, (AlterSystemStatementTestCase) expected);
        } else if (actual instanceof OracleAnalyzeStatement) {
            AnalyzeStatementAssert.assertIs(assertContext, (OracleAnalyzeStatement) actual, (AnalyzeStatementTestCase) expected);
        } else if (actual instanceof OracleAssociateStatisticsStatement) {
            AssociateStatisticsStatementAssert.assertIs(assertContext, (OracleAssociateStatisticsStatement) actual, (AssociateStatisticsStatementTestCase) expected);
        } else if (actual instanceof OracleDisassociateStatisticsStatement) {
            DisassociateStatisticsStatementAssert.assertIs(assertContext, (OracleDisassociateStatisticsStatement) actual, (DisassociateStatisticsStatementTestCase) expected);
        } else if (actual instanceof OracleAuditStatement) {
            AuditStatementAssert.assertIs(assertContext, (OracleAuditStatement) actual, (AuditStatementTestCase) expected);
        } else if (actual instanceof OracleNoAuditStatement) {
            NoAuditStatementAssert.assertIs(assertContext, (OracleNoAuditStatement) actual, (NoAuditStatementTestCase) expected);
        } else if (actual instanceof OpenGaussCursorStatement) {
            CursorStatementAssert.assertIs(assertContext, (OpenGaussCursorStatement) actual, (CursorStatementTestCase) expected);
        } else if (actual instanceof DeclareStatement) {
            DeclareStatementAssert.assertIs(assertContext, (DeclareStatement) actual, (DeclareStatementTestCase) expected);
        } else if (actual instanceof CloseStatement) {
            CloseStatementAssert.assertIs(assertContext, (CloseStatement) actual, (CloseStatementTestCase) expected);
        } else if (actual instanceof MoveStatement) {
            MoveStatementAssert.assertIs(assertContext, (MoveStatement) actual, (MoveStatementTestCase) expected);
        } else if (actual instanceof FetchStatement) {
            FetchStatementAssert.assertIs(assertContext, (FetchStatement) actual, (FetchStatementTestCase) expected);
        } else if (actual instanceof ClusterStatement) {
            ClusterStatementAssert.assertIs(assertContext, (ClusterStatement) actual, (ClusterStatementTestCase) expected);
        } else if (actual instanceof CommentStatement) {
            CommentStatementAssert.assertIs(assertContext, (CommentStatement) actual, (CommentStatementTestCase) expected);
        } else if (actual instanceof ListenStatement) {
            ListenStatementAssert.assertIs(assertContext, (ListenStatement) actual, (ListenStatementTestCase) expected);
        } else if (actual instanceof UnlistenStatement) {
            UnlistenStatementAssert.assertIs(assertContext, (UnlistenStatement) actual, (UnlistenStatementTestCase) expected);
        } else if (actual instanceof NotifyStmtStatement) {
            NotifyStmtStatementAssert.assertIs(assertContext, (NotifyStmtStatement) actual, (NotifyStmtStatementTestCase) expected);
        } else if (actual instanceof RefreshMatViewStmtStatement) {
            RefreshMatViewStmtStatementAssert.assertIs(assertContext, (RefreshMatViewStmtStatement) actual, (RefreshMatViewStmtStatementTestCase) expected);
        } else if (actual instanceof ReindexStatement) {
            ReindexStatementAssert.assertIs(assertContext, (ReindexStatement) actual, (ReindexStatementTestCase) expected);
        } else if (actual instanceof SecurityLabelStmtStatement) {
            SecurityLabelStmtStatementAssert.assertIs(assertContext, (SecurityLabelStmtStatement) actual, (SecurityLabelStmtStatementTestCase) expected);
        }
    }
}
