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

package io.shardingsphere.core.parsing.antlr.parser;

import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.statement.ResetParamStatement;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.statement.SetParamStatement;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.statement.ShowStatement;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingUnsupportedException;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.DDLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.parsing.parser.sql.tcl.TCLStatement;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * SQL statement type.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public enum SQLStatementType {
    
    CREATE_TABLE("CreateTable", CreateTableStatement.class),
    
    ALTER_TABLE("AlterTable", AlterTableStatement.class),
    
    DROP_TABLE("DropTable", DDLStatement.class),
    
    TRUNCATE_TABLE("TruncateTable", DDLStatement.class),
    
    CREATE_INDEX("CreateIndex", DDLStatement.class),
    
    ALTER_INDEX("AlterIndex", DDLStatement.class),
    
    DROP_INDEX("DropIndex", DDLStatement.class),
    
    SELECT("Select", SelectStatement.class),
    
    SET_TRANSACTION("SetTransaction", TCLStatement.class),
    
    COMMIT("Commit", TCLStatement.class),
    
    ROLLBACK("Rollback", TCLStatement.class),
    
    SAVEPOINT("Savepoint", TCLStatement.class),
    
    BEGIN_WORK("BeginWork", TCLStatement.class),
    
    SET_VARIABLE("SetVariable", TCLStatement.class),
    
    SHOW("Show", ShowStatement.class),
    
    SET_PARAM("SetParam", SetParamStatement.class),
    
    RESET_PARAM("ResetParam", ResetParamStatement.class);
    
    private final String name;
    
    private final Class<? extends SQLStatement> clazz;
    
    /**
     * Create new instance for SQL statement.
     * 
     * @return new instance for SQL statement
     */
    @SneakyThrows
    public SQLStatement newSQLStatement() {
        return clazz.newInstance();
    }
    
    /**
     * Get SQL statement type via name.
     * 
     * @param name name of SQL statement type
     * @return SQL statement type
     */
    public static SQLStatementType nameOf(final String name) {
        for (SQLStatementType each : SQLStatementType.values()) {
            if ((each.name + "Context").equals(name)) {
                return each;
            }
        }
        throw new SQLParsingUnsupportedException(String.format("Unsupported SQL statement of `%s`", name));
    }
}
