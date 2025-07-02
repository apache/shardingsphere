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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CommentStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CursorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.MoveStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.CreateSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.synonym.AlterSynonymStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.tablespace.AlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.RefreshMatViewStmtStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.OracleAlterSessionStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.OracleAlterSystemStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.OracleAnalyzeStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.OracleAuditStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.OracleNoAuditStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.OraclePurgeStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.flashback.OracleFlashbackTableStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.statistics.OracleAssociateStatisticsStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.statistics.OracleDisassociateStatisticsStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLClusterStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDeclareStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLListenStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLNotifyStmtStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLOpenStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLReindexStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLSecurityLabelStmtStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLUnlistenStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.ddl.statistics.SQLServerUpdateStatisticsStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.AlterIndexStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.AlterSessionStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.AlterSynonymStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.AlterSystemStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.AlterTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.AlterTablespaceStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.AlterViewStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.AnalyzeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.AssociateStatisticsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.AuditStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.CloseStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.ClusterStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.CommentStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.CreateIndexStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.CreateSequenceStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.CreateTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.CreateViewStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.CursorStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.DeclareStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.DisassociateStatisticsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.DropIndexStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.DropTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.DropViewStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.FetchStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.FlashbackTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.ListenStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.MoveStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.NoAuditStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.NotifyStmtStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.OpenStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.PurgeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.RefreshMatViewStmtStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.ReindexStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.RenameTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.SecurityLabelStmtStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.TruncateStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.UnlistenStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl.UpdateStatisticsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.AlterIndexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.AlterSessionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.AlterSynonymStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.AlterSystemStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.AlterTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.AlterTablespaceStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.AlterViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.AnalyzeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.AssociateStatisticsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.AuditStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.CloseStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.ClusterStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.CommentStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.CreateIndexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.CreateSequenceStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.CreateTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.CreateViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.CursorStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.DeclareStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.DisassociateStatisticsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.DropIndexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.DropTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.DropViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.FetchStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.FlashbackTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.ListenStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.MoveStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.NoAuditStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.NotifyStmtStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.OpenStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.PurgeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.RefreshMatViewStmtStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.ReindexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.RenameTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.SecurityLabelStmtStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.TruncateStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.UnlistenStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.UpdateStatisticsStatementTestCase;

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
        } else if (actual instanceof AlterSynonymStatement) {
            AlterSynonymStatementAssert.assertIs(assertContext, (AlterSynonymStatement) actual, (AlterSynonymStatementTestCase) expected);
        } else if (actual instanceof OracleAlterSessionStatement) {
            AlterSessionStatementAssert.assertIs(assertContext, (OracleAlterSessionStatement) actual, (AlterSessionStatementTestCase) expected);
        } else if (actual instanceof OracleAlterSystemStatement) {
            AlterSystemStatementAssert.assertIs(assertContext, (OracleAlterSystemStatement) actual, (AlterSystemStatementTestCase) expected);
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
        } else if (actual instanceof CursorStatement) {
            CursorStatementAssert.assertIs(assertContext, (CursorStatement) actual, (CursorStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLDeclareStatement) {
            DeclareStatementAssert.assertIs(assertContext, (PostgreSQLDeclareStatement) actual, (DeclareStatementTestCase) expected);
        } else if (actual instanceof CloseStatement) {
            CloseStatementAssert.assertIs(assertContext, (CloseStatement) actual, (CloseStatementTestCase) expected);
        } else if (actual instanceof MoveStatement) {
            MoveStatementAssert.assertIs(assertContext, (MoveStatement) actual, (MoveStatementTestCase) expected);
        } else if (actual instanceof FetchStatement) {
            FetchStatementAssert.assertIs(assertContext, (FetchStatement) actual, (FetchStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLClusterStatement) {
            ClusterStatementAssert.assertIs(assertContext, (PostgreSQLClusterStatement) actual, (ClusterStatementTestCase) expected);
        } else if (actual instanceof CommentStatement) {
            CommentStatementAssert.assertIs(assertContext, (CommentStatement) actual, (CommentStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLListenStatement) {
            ListenStatementAssert.assertIs(assertContext, (PostgreSQLListenStatement) actual, (ListenStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLUnlistenStatement) {
            UnlistenStatementAssert.assertIs(assertContext, (PostgreSQLUnlistenStatement) actual, (UnlistenStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLNotifyStmtStatement) {
            NotifyStmtStatementAssert.assertIs(assertContext, (PostgreSQLNotifyStmtStatement) actual, (NotifyStmtStatementTestCase) expected);
        } else if (actual instanceof RefreshMatViewStmtStatement) {
            RefreshMatViewStmtStatementAssert.assertIs(assertContext, (RefreshMatViewStmtStatement) actual, (RefreshMatViewStmtStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLReindexStatement) {
            ReindexStatementAssert.assertIs(assertContext, (PostgreSQLReindexStatement) actual, (ReindexStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLSecurityLabelStmtStatement) {
            SecurityLabelStmtStatementAssert.assertIs(assertContext, (PostgreSQLSecurityLabelStmtStatement) actual, (SecurityLabelStmtStatementTestCase) expected);
        } else if (actual instanceof CreateViewStatement) {
            CreateViewStatementAssert.assertIs(assertContext, (CreateViewStatement) actual, (CreateViewStatementTestCase) expected);
        } else if (actual instanceof AlterViewStatement) {
            AlterViewStatementAssert.assertIs(assertContext, (AlterViewStatement) actual, (AlterViewStatementTestCase) expected);
        } else if (actual instanceof DropViewStatement) {
            DropViewStatementAssert.assertIs(assertContext, (DropViewStatement) actual, (DropViewStatementTestCase) expected);
        } else if (actual instanceof AlterTablespaceStatement) {
            AlterTablespaceStatementAssert.assertIs(assertContext, (AlterTablespaceStatement) actual, (AlterTablespaceStatementTestCase) expected);
        } else if (actual instanceof CreateSequenceStatement) {
            CreateSequenceStatementAssert.assertIs(assertContext, (CreateSequenceStatement) actual, (CreateSequenceStatementTestCase) expected);
        } else if (actual instanceof SQLServerUpdateStatisticsStatement) {
            UpdateStatisticsStatementAssert.assertIs(assertContext, (SQLServerUpdateStatisticsStatement) actual, (UpdateStatisticsStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLOpenStatement) {
            OpenStatementAssert.assertIs(assertContext, (PostgreSQLOpenStatement) actual, (OpenStatementTestCase) expected);
        } else if (actual instanceof OracleFlashbackTableStatement) {
            FlashbackTableStatementAssert.assertIs(assertContext, (OracleFlashbackTableStatement) actual, (FlashbackTableStatementTestCase) expected);
        } else if (actual instanceof OraclePurgeStatement) {
            PurgeStatementAssert.assertIs(assertContext, (OraclePurgeStatement) actual, (PurgeStatementTestCase) expected);
        }
    }
}
