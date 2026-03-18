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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare;

import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.AlterUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.DropUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.CallStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DoStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XABeginStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XACommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XAEndStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XAPrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XARecoveryStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XARollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLFlushStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLKillStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLResetStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.index.MySQLCacheIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.index.MySQLLoadIndexInfoStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.plugin.MySQLInstallPluginStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.plugin.MySQLUninstallPluginStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.replication.binlog.MySQLShowBinaryLogsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.replication.binlog.MySQLShowBinlogEventsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.replication.opertation.MySQLChangeMasterStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.replication.opertation.MySQLStartSlaveStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.replication.opertation.MySQLStopSlaveStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.replication.show.MySQLShowStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.error.MySQLShowErrorsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.error.MySQLShowWarningsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.event.MySQLShowCreateEventStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.function.MySQLShowCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.procedure.MySQLShowCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.view.MySQLShowCreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.table.MySQLChecksumTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.table.MySQLOptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.table.MySQLRepairTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.MySQLGrantStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.MySQLRevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.user.MySQLCreateUserStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.user.MySQLRenameUserStatement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MySQLComStmtPrepareCheckerTest {
    
    @Test
    void assertIsStatementAllowed() {
        Collection<SQLStatement> sqlStatements = Arrays.<SQLStatement>asList(
                mock(AlterTableStatement.class), mock(AlterUserStatement.class), mock(AnalyzeTableStatement.class), mock(MySQLCacheIndexStatement.class),
                mock(CallStatement.class), mock(MySQLChangeMasterStatement.class), mock(MySQLChecksumTableStatement.class), mock(CommitStatement.class),
                mock(CreateIndexStatement.class), mock(DropIndexStatement.class), mock(CreateDatabaseStatement.class), mock(DropDatabaseStatement.class), mock(CreateTableStatement.class),
                mock(DropTableStatement.class), mock(MySQLCreateUserStatement.class), mock(MySQLRenameUserStatement.class), mock(DropUserStatement.class),
                mock(CreateViewStatement.class), mock(DropViewStatement.class), mock(DeleteStatement.class), mock(DoStatement.class), mock(MySQLFlushStatement.class),
                mock(MySQLGrantStatement.class), mock(InsertStatement.class), mock(MySQLInstallPluginStatement.class), mock(MySQLKillStatement.class),
                mock(MySQLLoadIndexInfoStatement.class), mock(MySQLOptimizeTableStatement.class), mock(RenameTableStatement.class), mock(MySQLRepairTableStatement.class),
                mock(MySQLResetStatement.class), mock(MySQLRevokeStatement.class), mock(SelectStatement.class), mock(SetStatement.class), mock(MySQLShowWarningsStatement.class),
                mock(MySQLShowErrorsStatement.class), mock(MySQLShowBinlogEventsStatement.class), mock(MySQLShowCreateProcedureStatement.class), mock(MySQLShowCreateFunctionStatement.class),
                mock(MySQLShowCreateEventStatement.class), mock(MySQLShowCreateTableStatement.class), mock(MySQLShowCreateViewStatement.class), mock(MySQLShowBinaryLogsStatement.class),
                mock(MySQLShowStatusStatement.class), mock(MySQLStartSlaveStatement.class), mock(MySQLStopSlaveStatement.class), mock(TruncateStatement.class),
                mock(MySQLUninstallPluginStatement.class), mock(UpdateStatement.class), mock(XABeginStatement.class), mock(XAPrepareStatement.class), mock(XACommitStatement.class),
                mock(XARollbackStatement.class), mock(XAEndStatement.class), mock(XARecoveryStatement.class));
        for (SQLStatement each : sqlStatements) {
            assertTrue(MySQLComStmtPrepareChecker.isAllowedStatement(each));
        }
    }
}
