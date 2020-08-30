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

import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.CacheIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ChecksumTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.FlushStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.InstallPluginStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.KillStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.LoadIndexInfoStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.OptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.RepairTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ResetStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowBinaryLogsStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowBinlogStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowCreateEventStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowCreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowErrorsStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowWarningsStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.UninstallPluginStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.AlterUserStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.CreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.DropUserStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.RenameUserStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.RevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropViewStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.CallStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DoStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.rl.ChangeMasterStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.rl.StartSlaveStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.rl.StopSlaveStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.CommitStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class MySQLComStmtPrepareCheckerTest {
    
    @Test
    public void assertIsStatementAllowed() {
        List<SQLStatement> statementList = Arrays.asList(new AlterTableStatement(mock(SimpleTableSegment.class)), new AlterUserStatement(), new AnalyzeTableStatement(), new CacheIndexStatement(),
            new CallStatement(), new ChangeMasterStatement(), new ChecksumTableStatement(), new CommitStatement(), new CreateIndexStatement(), new DropIndexStatement(),
            new CreateDatabaseStatement(""), new DropDatabaseStatement(), new CreateTableStatement(mock(SimpleTableSegment.class)), new DropTableStatement(), new CreateUserStatement(),
            new RenameUserStatement(), new DropUserStatement(), new CreateViewStatement(), new DropViewStatement(), new DeleteStatement(), new DoStatement(), new FlushStatement(),
            new GrantStatement(), new InsertStatement(), new InstallPluginStatement(), new KillStatement(), new LoadIndexInfoStatement(), new OptimizeTableStatement(), new RenameTableStatement(),
            new RepairTableStatement(), new ResetStatement(), new RevokeStatement(), new SelectStatement(), new SetStatement(), new ShowWarningsStatement(),
            new ShowErrorsStatement(), new ShowBinlogStatement(), new ShowCreateProcedureStatement(), new ShowCreateFunctionStatement(), new ShowCreateEventStatement(),
            new ShowCreateTableStatement(), new ShowCreateViewStatement(), new ShowBinaryLogsStatement(), new ShowStatusStatement(), new StartSlaveStatement(), new StopSlaveStatement(),
            new TruncateStatement(), new UninstallPluginStatement(), new UpdateStatement());
        for (SQLStatement each : statementList) {
            assertTrue(MySQLComStmtPrepareChecker.isStatementAllowed(each));
        }
    }
}
