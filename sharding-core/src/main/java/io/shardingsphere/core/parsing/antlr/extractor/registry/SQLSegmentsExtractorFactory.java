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

package io.shardingsphere.core.parsing.antlr.extractor.registry;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antlr.extractor.SQLStatementExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.registry.dialect.MySQLSegmentsExtractorRegistry;
import io.shardingsphere.core.parsing.antlr.extractor.registry.dialect.OracleSegmentsExtractorRegistry;
import io.shardingsphere.core.parsing.antlr.extractor.registry.dialect.PostgreSQLSegmentsExtractorRegistry;
import io.shardingsphere.core.parsing.antlr.extractor.registry.dialect.SQLServerSegmentsExtractorRegistry;
import io.shardingsphere.core.parsing.antlr.parser.SQLStatementType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL segments extractor factory.
 * 
 * @author duhongjun
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLSegmentsExtractorFactory {
    
    private static final Map<DatabaseType, SQLSegmentsExtractorRegistry> EXTRACTOR_REGISTRY = new HashMap<>(5, 1);
    
    static {
        EXTRACTOR_REGISTRY.put(DatabaseType.H2, new MySQLSegmentsExtractorRegistry());
        EXTRACTOR_REGISTRY.put(DatabaseType.MySQL, new MySQLSegmentsExtractorRegistry());
        EXTRACTOR_REGISTRY.put(DatabaseType.PostgreSQL, new PostgreSQLSegmentsExtractorRegistry());
        EXTRACTOR_REGISTRY.put(DatabaseType.SQLServer, new SQLServerSegmentsExtractorRegistry());
        EXTRACTOR_REGISTRY.put(DatabaseType.Oracle, new OracleSegmentsExtractorRegistry());
    }
    
    /**
     * Get SQL segments extractor.
     * 
     * @param databaseType database type
     * @param sqlStatementType SQL statement type
     * @return SQL segments extractor
     */
    public static Optional<SQLStatementExtractor> getInstance(final DatabaseType databaseType, final SQLStatementType sqlStatementType) {
        return Optional.fromNullable(EXTRACTOR_REGISTRY.get(databaseType).getExtractor(sqlStatementType));
    }
}
