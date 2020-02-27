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

package org.apache.shardingsphere.shardingproxy.frontend.mysql.command.query.binary.prepare;

import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.frontend.api.CommandExecutor;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.MySQLBinaryStatementRegistry;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.prepare.MySQLComStmtPrepareOKPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.packet.DatabasePacket;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * COM_STMT_PREPARE command executor for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/5.7/en/sql-prepared-statements.html">SQL Syntax Allowed in Prepared Statements</a>
 */
public final class MySQLComStmtPrepareExecutor implements CommandExecutor {
    
    private static final Set<String> SQL_SYNTAX_ALLOWED = new HashSet<>();
    
    private static final int MAX_CHECK_TOKENS = "FLUSH TABLES WITH READ LOCK".split("\\W+").length;
    
    private static final MySQLBinaryStatementRegistry PREPARED_STATEMENT_REGISTRY = MySQLBinaryStatementRegistry.getInstance();
    
    private final MySQLComStmtPreparePacket packet;
    
    private final LogicSchema logicSchema;
    
    static {
        SQL_SYNTAX_ALLOWED.addAll(Arrays.asList("ALTER" + "TABLE", "ALTER" + "USER", "ANALYZE" + "TABLE",
            "CACHE" + "INDEX", "CALL", "CHANGE" + "MASTER", "CHECKSUM" + "TABLE", "CHECKSUM" + "TABLES",
            "COMMIT", "CREATE" + "INDEX", "DROP" + "INDEX", "CREATE" + "DATABASE", "RENAME" + "DATABASE",
            "DROP" + "DATABASE", "CREATE" + "TABLE", "DROP" + "TABLE", "CREATE" + "USER", "RENAME" + "USER",
            "DROP" + "USER", "CREATE" + "VIEW", "DROP" + "VIEW", "DELETE", "DO", "FLUSH" + "TABLE",
            "FLUSH" + "TABLES", "FLUSH" + "TABLES" + "WITH" + "READ" + "LOCK", "FLUSH" + "HOSTS",
            "FLUSH" + "PRIVILEGES", "FLUSH" + "LOGS", "FLUSH" + "STATUS", "FLUSH" + "MASTER", "FLUSH" + "SLAVE",
            "FLUSH" + "DES_KEY_FILE", "FLUSH" + "USER" + "RESOURCES", "GRANT", "INSERT", "INSTALL" + "PLUGIN",
            "KILL", "LOAD" + "INDEX" + "INTO" + "CACHE", "OPTIMIZE" + "TABLE", "RENAME" + "TABLE",
            "REPAIR" + "TABLE", "REPLACE", "RESET" + "MASTER", "RESET" + "SLAVE", "RESET" + "QUERY CACHE",
            "REVOKE", "SELECT", "SET", "SHOW" + "WARNINGS", "SHOW" + "ERRORS", "SHOW" + "BINLOG" + "EVENTS",
            "SHOW" + "CREATE" + "PROCEDURE", "SHOW" + "CREATE" + "FUNCTION", "SHOW" + "CREATE" + "EVENT",
            "SHOW" + "CREATE" + "TABLE", "SHOW" + "CREATE" + "VIEW", "SHOW" + "MASTER" + "LOGS",
            "SHOW" + "BINARY" + "LOGS", "SHOW" + "MASTER" + "STATUS", "SHOW" + "SLAVE" + "STATUS",
            "SLAVE" + "START", "SLAVE" + "STOP", "TRUNCATE" + "TABLE", "UNINSTALL" + "PLUGIN", "UPDATE"));
    }
    
    public MySQLComStmtPrepareExecutor(final MySQLComStmtPreparePacket packet, final BackendConnection backendConnection) {
        this.packet = packet;
        logicSchema = backendConnection.getLogicSchema();
    }
    
    private boolean isSQLAllowed() {
        String[] tokens = packet.getSql().split("\\W+", MAX_CHECK_TOKENS + 1);
        StringBuilder sqlSyntax = new StringBuilder();
        for (String token : tokens) {
            sqlSyntax.append(token.toUpperCase());
            if (SQL_SYNTAX_ALLOWED.contains(sqlSyntax.toString())) {
                return true;
            }
        }
        return false;
    }
    
    private int getColumnsCount(final SQLStatement sqlStatement) {
        return sqlStatement instanceof SelectStatement ? ((SelectStatement) sqlStatement).getProjections().getProjections().size() : 0;
    }
    
    @Override
    public Collection<DatabasePacket> execute() {
        Collection<DatabasePacket> result = new LinkedList<>();
        int currentSequenceId = 0;
        if (!isSQLAllowed()) {
            result.add(new MySQLErrPacket(++currentSequenceId, MySQLServerErrorCode.ER_UNSUPPORTED_PS));
            return result;
        }
        SQLStatement sqlStatement = logicSchema.getSqlParserEngine().parse(packet.getSql(), true);
        int parametersCount = sqlStatement.getParametersCount();
        int columnsCount = getColumnsCount(sqlStatement);
        result.add(new MySQLComStmtPrepareOKPacket(++currentSequenceId, PREPARED_STATEMENT_REGISTRY.register(packet.getSql(), parametersCount), columnsCount, parametersCount, 0));
        if (parametersCount > 0) {
            for (int i = 0; i < parametersCount; i++) {
                result.add(new MySQLColumnDefinition41Packet(++currentSequenceId, "", "", "", "?", "", 0, MySQLColumnType.MYSQL_TYPE_VAR_STRING, 0));
            }
            result.add(new MySQLEofPacket(++currentSequenceId));
        }
        if (columnsCount > 0) {
            for (int i = 0; i < columnsCount; i++) {
                result.add(new MySQLColumnDefinition41Packet(++currentSequenceId, "", "", "", "", "", 0, MySQLColumnType.MYSQL_TYPE_VAR_STRING, 0));
            }
            result.add(new MySQLEofPacket(++currentSequenceId));
        }
        return result;
    }
}
