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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * COM_STMT_PREPARE command statement checker for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/5.7/en/sql-prepared-statements.html">SQL Syntax Allowed in Prepared Statements</a>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLComStmtPrepareChecker {
    
    private static final Set<Class> SQL_STATEMENTS_ALLOWED = new HashSet<>();
    
    static {
        SQL_STATEMENTS_ALLOWED.addAll(Arrays.asList(AlterTableStatement.class, AlterUserStatement.class, AnalyzeTableStatement.class,
            CacheIndexStatement.class, CallStatement.class, ChangeMasterStatement.class, ChecksumTableStatement.class, CommitStatement.class,
            CreateIndexStatement.class, DropIndexStatement.class, CreateDatabaseStatement.class, DropDatabaseStatement.class,
            CreateTableStatement.class, DropTableStatement.class, CreateUserStatement.class, RenameUserStatement.class, DropUserStatement.class,
            CreateViewStatement.class, DropViewStatement.class, DeleteStatement.class, DoStatement.class, FlushStatement.class,
            GrantStatement.class, InsertStatement.class, InstallPluginStatement.class, KillStatement.class, LoadIndexInfoStatement.class,
            OptimizeTableStatement.class, RenameTableStatement.class, RepairTableStatement.class, ResetStatement.class,
            RevokeStatement.class, SelectStatement.class, SetStatement.class, ShowWarningsStatement.class, ShowErrorsStatement.class,
            ShowBinlogStatement.class, ShowCreateProcedureStatement.class, ShowCreateFunctionStatement.class, ShowCreateEventStatement.class,
            ShowCreateTableStatement.class, ShowCreateViewStatement.class, ShowBinaryLogsStatement.class, ShowStatusStatement.class,
            StartSlaveStatement.class, StopSlaveStatement.class, TruncateStatement.class, UninstallPluginStatement.class, UpdateStatement.class));
    }
    
    /**
     * Judge if SQL statement is allowed.
     *
     * @param sqlStatement SQL statement
     * @return sql statement is allowed or not
     */
    public static boolean isStatementAllowed(final SQLStatement sqlStatement) {
        return SQL_STATEMENTS_ALLOWED.contains(sqlStatement.getClass());
    }
}
