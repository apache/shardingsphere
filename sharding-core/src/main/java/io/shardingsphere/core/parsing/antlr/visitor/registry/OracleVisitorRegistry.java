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

import io.shardingsphere.core.parsing.antlr.visitor.SQLStatementType;
import io.shardingsphere.core.parsing.antlr.visitor.statement.CreateTableVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.IndexWithTableStatementVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.OnlySingleTableVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.StatementVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.TCLStatementVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.dialect.oracle.OracleAlterIndexVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.dialect.oracle.OracleAlterTableVisitor;
import io.shardingsphere.core.parsing.antlr.visitor.statement.dialect.oracle.OracleDropIndexVisitor;

import java.util.HashMap;
import java.util.Map;

/**
 * Visitor registry for Oracle.
 * 
 * @author duhongjun
 * @author zhangliang
 */
public final class OracleVisitorRegistry implements DatabaseVisitorRegistry {
    
    private static final Map<SQLStatementType, StatementVisitor> VISITORS = new HashMap<>();
    
    static {
        registerDDL();
        registerTCL();
    }
    
    private static void registerDDL() {
        VISITORS.put(SQLStatementType.CREATE_TABLE, new CreateTableVisitor());
        VISITORS.put(SQLStatementType.ALTER_TABLE, new OracleAlterTableVisitor());
        VISITORS.put(SQLStatementType.DROP_TABLE, new OnlySingleTableVisitor());
        VISITORS.put(SQLStatementType.TRUNCATE_TABLE, new OnlySingleTableVisitor());
        VISITORS.put(SQLStatementType.CREATE_INDEX, new IndexWithTableStatementVisitor());
        VISITORS.put(SQLStatementType.ALTER_INDEX, new OracleAlterIndexVisitor());
        VISITORS.put(SQLStatementType.DROP_INDEX, new OracleDropIndexVisitor());
    }
    
    private static void registerTCL() {
        VISITORS.put(SQLStatementType.SET_TRANSACTION, new TCLStatementVisitor());
        VISITORS.put(SQLStatementType.COMMIT, new TCLStatementVisitor());
        VISITORS.put(SQLStatementType.ROLLBACK, new TCLStatementVisitor());
        VISITORS.put(SQLStatementType.SAVEPOINT, new TCLStatementVisitor());
        VISITORS.put(SQLStatementType.BEGIN_WORK, new TCLStatementVisitor());
    }
    
    @Override
    public StatementVisitor getVisitor(final SQLStatementType type) {
        return VISITORS.get(type);
    }
}
