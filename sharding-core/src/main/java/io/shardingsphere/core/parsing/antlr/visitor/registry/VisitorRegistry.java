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

package io.shardingsphere.core.parsing.antlr.visitor.registry;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antlr.visitor.SQLStatementType;
import io.shardingsphere.core.parsing.antlr.visitor.statement.StatementVisitor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Visitor registry.
 * 
 * @author duhongjun
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VisitorRegistry {
    
    private static final Map<DatabaseType, DatabaseVisitorRegistry> VISITORS_REGISTRY = new HashMap<>();
    
    static {
        VISITORS_REGISTRY.put(DatabaseType.H2, new H2VisitorRegistry());
        VISITORS_REGISTRY.put(DatabaseType.MySQL, new MySQLVisitorRegistry());
        VISITORS_REGISTRY.put(DatabaseType.PostgreSQL, new PostgreSQLVisitorRegistry());
        VISITORS_REGISTRY.put(DatabaseType.SQLServer, new SQLServerVisitorRegistry());
        VISITORS_REGISTRY.put(DatabaseType.Oracle, new OracleVisitorRegistry());
    }
    
    /**
     * Get statement visitor.
     * 
     * @param databaseType database type
     * @param sqlStatementType SQL statement type
     * @return statement visitor
     */
    public static StatementVisitor getVisitor(final DatabaseType databaseType, final SQLStatementType sqlStatementType) {
        return VISITORS_REGISTRY.get(databaseType).getVisitor(sqlStatementType);
    }
}
