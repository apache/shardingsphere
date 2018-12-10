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

package io.shardingsphere.core.parsing.antlr.optimizer.registry.dialect;

import io.shardingsphere.core.parsing.antlr.parser.SQLStatementType;
import io.shardingsphere.core.parsing.antlr.optimizer.impl.SQLStatementOptimizer;
import io.shardingsphere.core.parsing.antlr.optimizer.impl.ddl.CreateTableOptimizer;
import io.shardingsphere.core.parsing.antlr.optimizer.impl.ddl.dialect.mysql.MySQLAlterTableOptimizer;
import io.shardingsphere.core.parsing.antlr.optimizer.impl.dql.dialect.mysql.MySQLSelectOptimizer;
import io.shardingsphere.core.parsing.antlr.optimizer.registry.SQLStatementOptimizerRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL statement optimizer for MySQL.
 * 
 * @author duhongjun
 * @author zhangliang
 */
public final class MySQLOptimizerRegistry implements SQLStatementOptimizerRegistry {
    
    private static final Map<SQLStatementType, SQLStatementOptimizer> OPTIMIZER = new HashMap<>();
    
    static {
        registerDDL();
        registerDQL();
    }
    
    private static void registerDDL() {
        OPTIMIZER.put(SQLStatementType.CREATE_TABLE, new CreateTableOptimizer());
        OPTIMIZER.put(SQLStatementType.ALTER_TABLE, new MySQLAlterTableOptimizer());
    }
    
    private static void registerDQL() {
        OPTIMIZER.put(SQLStatementType.SELECT, new MySQLSelectOptimizer());
    }
    
    @Override
    public SQLStatementOptimizer getOptimizer(final SQLStatementType type) {
        return OPTIMIZER.get(type);
    }
}
