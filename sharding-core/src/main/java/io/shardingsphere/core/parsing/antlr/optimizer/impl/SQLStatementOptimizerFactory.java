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

package io.shardingsphere.core.parsing.antlr.optimizer.impl;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antlr.parser.SQLStatementType;
import io.shardingsphere.core.parsing.antlr.optimizer.registry.SQLStatementOptimizerRegistry;
import io.shardingsphere.core.parsing.antlr.optimizer.registry.dialect.MySQLOptimizerRegistry;
import io.shardingsphere.core.parsing.antlr.optimizer.registry.dialect.OracleOptimizerRegistry;
import io.shardingsphere.core.parsing.antlr.optimizer.registry.dialect.PostgreSQLOptimizerRegistry;
import io.shardingsphere.core.parsing.antlr.optimizer.registry.dialect.SQLServerOptimizerRegistry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL statement optimizer factory.
 * 
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementOptimizerFactory {
    
    private static final Map<DatabaseType, SQLStatementOptimizerRegistry> OPTIMIZER_REGISTRY = new HashMap<>(5, 1);
    
    static {
        OPTIMIZER_REGISTRY.put(DatabaseType.H2, new MySQLOptimizerRegistry());
        OPTIMIZER_REGISTRY.put(DatabaseType.MySQL, new MySQLOptimizerRegistry());
        OPTIMIZER_REGISTRY.put(DatabaseType.PostgreSQL, new PostgreSQLOptimizerRegistry());
        OPTIMIZER_REGISTRY.put(DatabaseType.SQLServer, new SQLServerOptimizerRegistry());
        OPTIMIZER_REGISTRY.put(DatabaseType.Oracle, new OracleOptimizerRegistry());
    }
    
    /**
     * Get SQL statement optimizer.
     * 
     * @param databaseType database type
     * @param sqlStatementType SQL statement type
     * @return SQL statement optimizer
     */
    public static Optional<SQLStatementOptimizer> getInstance(final DatabaseType databaseType, final SQLStatementType sqlStatementType) {
        return Optional.fromNullable(OPTIMIZER_REGISTRY.get(databaseType).getOptimizer(sqlStatementType));
    }
}
