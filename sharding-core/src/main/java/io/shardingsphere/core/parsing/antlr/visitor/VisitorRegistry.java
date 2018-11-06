/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antlr.visitor;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antlr.visitor.statement.CreateTableVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.IndexWithTableStatementVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.OnlyMultiTableVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.OnlySingleTableVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.StatementVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.TCLStatementVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.dialect.mysql.MySQLAlterTableVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.dialect.oracle.OracleAlterIndexVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.dialect.oracle.OracleAlterTableVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.dialect.oracle.OracleDropIndexVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.dialect.postgresql.PostgreSQLAlterIndexVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.dialect.postgresql.PostgreSQLAlterTableVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.dialect.sqlserver.SQLServerAlterTableVisitor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Visitor registry.
 * 
 * @author duhongjun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VisitorRegistry {
    
    private static final Map<String, StatementVisitor> VISITORS = new HashMap<>();
    
    static {
        registerDDLVisitor();
        registerTCLVisitor();
    }
    
    private static void registerDDLVisitor() {
        registerCommonDDL();
        registerMySQLDDL();
        registerOracleDDL();
        registerSQLServerDDL();
        registerPostgreSQLDDL();
    }
    
    private static void registerCommonDDL() {
        VISITORS.put("CreateTable", new CreateTableVisitor());
        VISITORS.put("DropTable", new OnlySingleTableVisitor());
        VISITORS.put("TruncateTable", new OnlySingleTableVisitor());
        VISITORS.put("CreateIndex", new IndexWithTableStatementVisitor());
    }
    
    private static void registerMySQLDDL() {
        VISITORS.put(DatabaseType.H2 + "AlterTable", new MySQLAlterTableVisitor());
        VISITORS.put(DatabaseType.H2 + "DropTable", new OnlyMultiTableVisitor());
        VISITORS.put(DatabaseType.H2 + "DropIndex", new IndexWithTableStatementVisitor());
        VISITORS.put(DatabaseType.MySQL + "AlterTable", new MySQLAlterTableVisitor());
        VISITORS.put(DatabaseType.MySQL + "DropTable", new OnlyMultiTableVisitor());
        VISITORS.put(DatabaseType.MySQL + "DropIndex", new IndexWithTableStatementVisitor());
    }
    
    private static void registerOracleDDL() {
        VISITORS.put(DatabaseType.Oracle + "AlterTable", new OracleAlterTableVisitor());
        VISITORS.put(DatabaseType.Oracle + "DropIndex", new OracleDropIndexVisitor());
        VISITORS.put(DatabaseType.Oracle + "AlterIndex", new OracleAlterIndexVisitor());
    }
    
    private static void registerSQLServerDDL() {
        VISITORS.put(DatabaseType.SQLServer + "AlterTable", new SQLServerAlterTableVisitor());
        VISITORS.put(DatabaseType.SQLServer + "DropTable", new OnlyMultiTableVisitor());
        VISITORS.put(DatabaseType.SQLServer + "DropIndex", new IndexWithTableStatementVisitor());
        VISITORS.put(DatabaseType.SQLServer + "AlterIndex", new IndexWithTableStatementVisitor());
    }
    
    private static void registerPostgreSQLDDL() {
        VISITORS.put(DatabaseType.PostgreSQL + "AlterTable", new PostgreSQLAlterTableVisitor());
        VISITORS.put(DatabaseType.PostgreSQL + "DropTable", new OnlyMultiTableVisitor());
        VISITORS.put(DatabaseType.PostgreSQL + "TruncateTable", new OnlyMultiTableVisitor());
        VISITORS.put(DatabaseType.PostgreSQL + "DropIndex", new IndexWithTableStatementVisitor());
        VISITORS.put(DatabaseType.PostgreSQL + "AlterIndex", new PostgreSQLAlterIndexVisitor());
    }
    
    private static void registerTCLVisitor() {
        VISITORS.put("SetTransaction", new TCLStatementVisitor());
        VISITORS.put("Commit", new TCLStatementVisitor());
        VISITORS.put("Rollback", new TCLStatementVisitor());
        VISITORS.put("Savepoint", new TCLStatementVisitor());
        VISITORS.put("BeginWork", new TCLStatementVisitor());
        VISITORS.put(DatabaseType.H2 + "SetVariable", new TCLStatementVisitor());
        VISITORS.put(DatabaseType.MySQL + "SetVariable", new TCLStatementVisitor());
    }
    
    /**
     * Get statement visitor.
     * 
     * @param databaseType database type
     * @param commandName command name
     * @return statement visitor
     */
    public static StatementVisitor getVisitor(final DatabaseType databaseType, final String commandName) {
        String key = databaseType.name() + commandName;
        return VISITORS.containsKey(key) ? VISITORS.get(key) : VISITORS.get(commandName);
    }
    
}
