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

import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.CallStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DoStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.rl.ChangeMasterStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.rl.StartSlaveStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.rl.StopSlaveStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLAnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCacheIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLChecksumTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLFlushStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLInstallPluginStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLKillStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLLoadIndexInfoStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLOptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLRepairTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLResetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowBinaryLogsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowBinlogStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateEventStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowErrorsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowWarningsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUninstallPluginStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLAlterUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLCreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLDropUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLGrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLRenameUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLRevokeStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class MySQLComStmtPrepareCheckerTest {
    
    @Test
    public void assertIsStatementAllowed() {
        List<SQLStatement> statementList = Arrays.asList(
            new AlterTableStatement(mock(SimpleTableSegment.class)), new MySQLAlterUserStatement(), new MySQLAnalyzeTableStatement(), new MySQLCacheIndexStatement(),
            new CallStatement(), new ChangeMasterStatement(), new MySQLChecksumTableStatement(), new CommitStatement(), new CreateIndexStatement(), new DropIndexStatement(),
            new CreateDatabaseStatement(""), new DropDatabaseStatement(""), new CreateTableStatement(mock(SimpleTableSegment.class)), new DropTableStatement(), new MySQLCreateUserStatement(),
            new MySQLRenameUserStatement(), new MySQLDropUserStatement(), new CreateViewStatement(), new DropViewStatement(), new DeleteStatement(), new DoStatement(), new MySQLFlushStatement(),
            new MySQLGrantStatement(), new InsertStatement(), new MySQLInstallPluginStatement(), new MySQLKillStatement(), 
            new MySQLLoadIndexInfoStatement(), new MySQLOptimizeTableStatement(), new RenameTableStatement(), new MySQLRepairTableStatement(), 
            new MySQLResetStatement(), new MySQLRevokeStatement(), new SelectStatement(), new SetStatement(), new MySQLShowWarningsStatement(),
            new MySQLShowErrorsStatement(), new MySQLShowBinlogStatement(), new MySQLShowCreateProcedureStatement(), new MySQLShowCreateFunctionStatement(), new MySQLShowCreateEventStatement(),
            new MySQLShowCreateTableStatement(), new MySQLShowCreateViewStatement(), new MySQLShowBinaryLogsStatement(), new MySQLShowStatusStatement(), new StartSlaveStatement(),
            new StopSlaveStatement(), new TruncateStatement(), new MySQLUninstallPluginStatement(), new UpdateStatement());
        for (SQLStatement each : statementList) {
            assertTrue(MySQLComStmtPrepareChecker.isStatementAllowed(each));
        }
    }
}
