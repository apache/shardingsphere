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

package io.shardingsphere.core.parsing.antlr.extractor.statement.registry.dialect;

import io.shardingsphere.core.parsing.antlr.extractor.statement.SQLSegmentsExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.SQLStatementType;
import io.shardingsphere.core.parsing.antlr.extractor.statement.engine.ddl.AlterIndexExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.engine.ddl.CreateIndexExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.engine.ddl.CreateTableExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.engine.ddl.DropIndexExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.engine.ddl.dialect.sqlserver.SQLServerAlterTableExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.engine.ddl.dialect.sqlserver.SQLServerDropTableExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.engine.ddl.dialect.sqlserver.SQLServerTruncateTableExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.engine.tcl.TCLSegmentsExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.registry.SQLSegmentsExtractorRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL segments extractor registry for SQLServer.
 * 
 * @author duhongjun
 * @author zhangliang
 */
public final class SQLServerSegmentsExtractorRegistry implements SQLSegmentsExtractorRegistry {
    
    private static final Map<SQLStatementType, SQLSegmentsExtractor> EXTRACTORS = new HashMap<>();
    
    static {
        registerDDL();
        registerTCL();
    }
    
    private static void registerDDL() {
        EXTRACTORS.put(SQLStatementType.CREATE_TABLE, new CreateTableExtractor());
        EXTRACTORS.put(SQLStatementType.ALTER_TABLE, new SQLServerAlterTableExtractor());
        EXTRACTORS.put(SQLStatementType.DROP_TABLE, new SQLServerDropTableExtractor());
        EXTRACTORS.put(SQLStatementType.TRUNCATE_TABLE, new SQLServerTruncateTableExtractor());
        EXTRACTORS.put(SQLStatementType.CREATE_INDEX, new CreateIndexExtractor());
        EXTRACTORS.put(SQLStatementType.ALTER_INDEX, new AlterIndexExtractor());
        EXTRACTORS.put(SQLStatementType.DROP_INDEX, new DropIndexExtractor());
    }
    
    private static void registerTCL() {
        EXTRACTORS.put(SQLStatementType.SET_TRANSACTION, new TCLSegmentsExtractor());
        EXTRACTORS.put(SQLStatementType.COMMIT, new TCLSegmentsExtractor());
        EXTRACTORS.put(SQLStatementType.ROLLBACK, new TCLSegmentsExtractor());
        EXTRACTORS.put(SQLStatementType.SAVEPOINT, new TCLSegmentsExtractor());
        EXTRACTORS.put(SQLStatementType.BEGIN_WORK, new TCLSegmentsExtractor());
    }
    
    @Override
    public SQLSegmentsExtractor getExtractor(final SQLStatementType type) {
        return EXTRACTORS.get(type);
    }
}
