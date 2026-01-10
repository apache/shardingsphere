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
        ALLOWED_SQL_STATEMENTS.addAll(Arrays.asList(AlterTableStatement.class, AlterUserStatement.class, AnalyzeTableStatement.class,
                MySQLCacheIndexStatement.class, CallStatement.class, MySQLChangeMasterStatement.class, MySQLChecksumTableStatement.class, CommitStatement.class,
                CreateIndexStatement.class, DropIndexStatement.class, CreateDatabaseStatement.class, DropDatabaseStatement.class,
                CreateTableStatement.class, DropTableStatement.class, MySQLCreateUserStatement.class, MySQLRenameUserStatement.class, DropUserStatement.class,
                CreateViewStatement.class, DropViewStatement.class, DeleteStatement.class, DoStatement.class, MySQLFlushStatement.class,
                MySQLGrantStatement.class, InsertStatement.class, MySQLInstallPluginStatement.class, MySQLKillStatement.class, MySQLLoadIndexInfoStatement.class,
                MySQLOptimizeTableStatement.class, RenameTableStatement.class, MySQLRepairTableStatement.class, MySQLResetStatement.class,
                MySQLRevokeStatement.class, SelectStatement.class, SetStatement.class, MySQLShowWarningsStatement.class, MySQLShowErrorsStatement.class,
                MySQLShowBinlogEventsStatement.class, MySQLShowCreateProcedureStatement.class, MySQLShowCreateFunctionStatement.class, MySQLShowCreateEventStatement.class,
                MySQLShowCreateTableStatement.class, MySQLShowCreateViewStatement.class, MySQLShowBinaryLogsStatement.class, MySQLShowStatusStatement.class,
                MySQLStartSlaveStatement.class, MySQLStopSlaveStatement.class, TruncateStatement.class, MySQLUninstallPluginStatement.class, UpdateStatement.class,
                XABeginStatement.class, XAPrepareStatement.class, XACommitStatement.class, XARollbackStatement.class, XAEndStatement.class, XARecoveryStatement.class));
        try {
            ALLOWED_SQL_STATEMENTS.add(Class.forName("org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisAnalyzeTableStatement"));
        } catch (final ClassNotFoundException ignored) {
        }
    }
    
    /**
     * Judge if SQL statement is allowed.
     *
     * @param sqlStatement SQL statement
     * @return SQL statement is allowed or not
     */
    public static boolean isAllowedStatement(final SQLStatement sqlStatement) {
        return ALLOWED_SQL_STATEMENTS.stream().anyMatch(each -> each.isAssignableFrom(sqlStatement.getClass()));
    }
}
