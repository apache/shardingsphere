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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.CacheIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ChecksumTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.FlushStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.InstallPluginStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.KillStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.LoadIndexInfoStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.OptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.RepairTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ResetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowBinaryLogsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowBinlogEventsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateEventStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowErrorsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowWarningsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.UninstallPluginStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.AlterUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.DropUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.RenameUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.CallStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DoStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.rl.ChangeMasterStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.rl.StartSlaveStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.rl.StopSlaveStatement;
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
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLAlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLDropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLDropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLUpdateStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * COM_STMT_PREPARE command statement checker for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/5.7/en/sql-prepared-statements.html">SQL Syntax Allowed in Prepared Statements</a>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLComStmtPrepareChecker {
    
    private static final Collection<Class<?>> ALLOWED_SQL_STATEMENTS = new HashSet<>();
    
    static {
        ALLOWED_SQL_STATEMENTS.addAll(Arrays.asList(MySQLAlterTableStatement.class, AlterUserStatement.class, AnalyzeTableStatement.class,
                CacheIndexStatement.class, CallStatement.class, ChangeMasterStatement.class, ChecksumTableStatement.class, CommitStatement.class,
                MySQLCreateIndexStatement.class, MySQLDropIndexStatement.class, CreateDatabaseStatement.class, DropDatabaseStatement.class,
                MySQLCreateTableStatement.class, MySQLDropTableStatement.class, MySQLCreateUserStatement.class, RenameUserStatement.class, DropUserStatement.class,
                CreateViewStatement.class, MySQLDropViewStatement.class, MySQLDeleteStatement.class, DoStatement.class, FlushStatement.class,
                MySQLGrantStatement.class, MySQLInsertStatement.class, InstallPluginStatement.class, KillStatement.class, LoadIndexInfoStatement.class,
                OptimizeTableStatement.class, RenameTableStatement.class, RepairTableStatement.class, ResetStatement.class,
                MySQLRevokeStatement.class, MySQLSelectStatement.class, SetStatement.class, ShowWarningsStatement.class, ShowErrorsStatement.class,
                ShowBinlogEventsStatement.class, ShowCreateProcedureStatement.class, ShowCreateFunctionStatement.class, ShowCreateEventStatement.class,
                ShowCreateTableStatement.class, ShowCreateViewStatement.class, ShowBinaryLogsStatement.class, ShowStatusStatement.class,
                StartSlaveStatement.class, StopSlaveStatement.class, TruncateStatement.class, UninstallPluginStatement.class, MySQLUpdateStatement.class,
                XABeginStatement.class, XAPrepareStatement.class, XACommitStatement.class, XARollbackStatement.class, XAEndStatement.class, XARecoveryStatement.class));
    }
    
    /**
     * Judge if SQL statement is allowed.
     *
     * @param sqlStatement SQL statement
     * @return SQL statement is allowed or not
     */
    public static boolean isAllowedStatement(final SQLStatement sqlStatement) {
        return ALLOWED_SQL_STATEMENTS.contains(sqlStatement.getClass());
    }
}
