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

package io.shardingsphere.core.parsing.antlr.filler.registry.dialect;

import io.shardingsphere.core.parsing.antlr.extractor.statement.SQLStatementType;
import io.shardingsphere.core.parsing.antlr.filler.registry.SQLStatementRegistry;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.DDLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;
import io.shardingsphere.core.parsing.parser.sql.tcl.TCLStatement;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL statement registry for Oracle.
 * 
 * @author zhangliang
 */
public final class OracleStatementRegistry implements SQLStatementRegistry {
    
    private static final Map<SQLStatementType, Class<? extends SQLStatement>> STATEMENTS = new HashMap<>();
    
    static {
        registerDDL();
        registerTCL();
    }
    
    private static void registerDDL() {
        STATEMENTS.put(SQLStatementType.CREATE_TABLE, CreateTableStatement.class);
        STATEMENTS.put(SQLStatementType.ALTER_TABLE, AlterTableStatement.class);
        STATEMENTS.put(SQLStatementType.DROP_TABLE, DDLStatement.class);
        STATEMENTS.put(SQLStatementType.TRUNCATE_TABLE, DDLStatement.class);
        STATEMENTS.put(SQLStatementType.CREATE_INDEX, DDLStatement.class);
        STATEMENTS.put(SQLStatementType.ALTER_INDEX, DDLStatement.class);
        STATEMENTS.put(SQLStatementType.DROP_INDEX, DDLStatement.class);
    }
    
    private static void registerTCL() {
        STATEMENTS.put(SQLStatementType.SET_TRANSACTION, TCLStatement.class);
        STATEMENTS.put(SQLStatementType.COMMIT, TCLStatement.class);
        STATEMENTS.put(SQLStatementType.ROLLBACK, TCLStatement.class);
        STATEMENTS.put(SQLStatementType.SAVEPOINT, TCLStatement.class);
        STATEMENTS.put(SQLStatementType.BEGIN_WORK, TCLStatement.class);
    }
    
    @Override
    @SneakyThrows
    public SQLStatement getSQLStatement(final SQLStatementType type) {
        return STATEMENTS.get(type).newInstance();
    }
}
