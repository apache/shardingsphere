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

package io.shardingsphere.core.parsing.antlr.filler;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antlr.extractor.statement.SQLStatementType;
import io.shardingsphere.core.parsing.antlr.filler.registry.SQLStatementRegistry;
import io.shardingsphere.core.parsing.antlr.filler.registry.dialect.MySQLStatementRegistry;
import io.shardingsphere.core.parsing.antlr.filler.registry.dialect.OracleStatementRegistry;
import io.shardingsphere.core.parsing.antlr.filler.registry.dialect.PostgreSQLStatementRegistry;
import io.shardingsphere.core.parsing.antlr.filler.registry.dialect.SQLServerStatementRegistry;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL statement factory.
 * 
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementFactory {
    
    private static final Map<DatabaseType, SQLStatementRegistry> STATEMENT_REGISTRY = new HashMap<>(5, 1);
    
    static {
        STATEMENT_REGISTRY.put(DatabaseType.H2, new MySQLStatementRegistry());
        STATEMENT_REGISTRY.put(DatabaseType.MySQL, new MySQLStatementRegistry());
        STATEMENT_REGISTRY.put(DatabaseType.PostgreSQL, new PostgreSQLStatementRegistry());
        STATEMENT_REGISTRY.put(DatabaseType.SQLServer, new SQLServerStatementRegistry());
        STATEMENT_REGISTRY.put(DatabaseType.Oracle, new OracleStatementRegistry());
    }
    
    /**
     * Get SQL statement.
     * 
     * @param databaseType database type
     * @param sqlStatementType SQL statement type
     * @return statement
     */
    public static SQLStatement getInstance(final DatabaseType databaseType, final SQLStatementType sqlStatementType) {
        return STATEMENT_REGISTRY.get(databaseType).getSQLStatement(sqlStatementType);
    }
}
