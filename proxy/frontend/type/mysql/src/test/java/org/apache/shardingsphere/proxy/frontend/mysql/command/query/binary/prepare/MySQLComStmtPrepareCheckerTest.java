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
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLCacheIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLChecksumTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.FlushStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLInstallPluginStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.KillStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLLoadIndexInfoStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.OptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLRepairTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLResetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowBinaryLogsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowBinlogEventsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowCreateEventStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowCreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowErrorsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowWarningsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLUninstallPluginStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.AlterUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.DropUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.RenameUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.CallStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DoStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLChangeMasterStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLStartSlaveStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLStopSlaveStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.xa.XABeginStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.xa.XACommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.xa.XAEndStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.xa.XAPrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.xa.XARecoveryStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.xa.XARollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.MySQLCreateUserStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.MySQLGrantStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.MySQLRevokeStatement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLComStmtPrepareCheckerTest {
    
    @Test
    void assertIsStatementAllowed() {
        CreateTableStatement createTableStatement = new CreateTableStatement();
        Collection<SQLStatement> sqlStatements = Arrays.asList(
                new AlterTableStatement(), new AlterUserStatement(), new AnalyzeTableStatement(Collections.emptyList()), new MySQLCacheIndexStatement(),
                new CallStatement(null, Collections.emptyList()), new MySQLChangeMasterStatement(), new MySQLChecksumTableStatement(Collections.emptyList()), new CommitStatement(),
                new CreateIndexStatement(), new DropIndexStatement(), new CreateDatabaseStatement(null, false), new DropDatabaseStatement(null, false), createTableStatement,
                new DropTableStatement(), new MySQLCreateUserStatement(), new RenameUserStatement(), new DropUserStatement(Collections.emptyList()),
                new CreateViewStatement(), new DropViewStatement(), new DeleteStatement(), new DoStatement(Collections.emptyList()), new FlushStatement(Collections.emptyList(), false),
                new MySQLGrantStatement(), new InsertStatement(), new MySQLInstallPluginStatement(null), new KillStatement(null, null),
                new MySQLLoadIndexInfoStatement(Collections.emptyList()), new OptimizeTableStatement(null), new RenameTableStatement(Collections.emptyList()),
                new MySQLRepairTableStatement(Collections.emptyList()),
                new MySQLResetStatement(Collections.emptyList()), new MySQLRevokeStatement(), new SelectStatement(), new SetStatement(Collections.emptyList()), new MySQLShowWarningsStatement(null),
                new MySQLShowErrorsStatement(null), new MySQLShowBinlogEventsStatement(null, null), new MySQLShowCreateProcedureStatement(null), new MySQLShowCreateFunctionStatement(null),
                new MySQLShowCreateEventStatement(null), new ShowCreateTableStatement(null), new MySQLShowCreateViewStatement(null), new MySQLShowBinaryLogsStatement(),
                new MySQLShowStatusStatement(null),
                new MySQLStartSlaveStatement(), new MySQLStopSlaveStatement(), new TruncateStatement(), new MySQLUninstallPluginStatement(null), new UpdateStatement(),
                new XABeginStatement("1"), new XAPrepareStatement("1"), new XACommitStatement("1"), new XARollbackStatement("1"), new XAEndStatement("1"), new XARecoveryStatement());
        for (SQLStatement each : sqlStatements) {
            assertTrue(MySQLComStmtPrepareChecker.isAllowedStatement(each));
        }
    }
}
