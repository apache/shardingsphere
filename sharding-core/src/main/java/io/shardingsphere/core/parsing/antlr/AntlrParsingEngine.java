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

package io.shardingsphere.core.parsing.antlr;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.ast.SQLAST;
import io.shardingsphere.core.parsing.antlr.ast.SQLParserEngine;
import io.shardingsphere.core.parsing.antlr.ast.SQLStatementType;
import io.shardingsphere.core.parsing.antlr.extractor.statement.SQLSegmentsExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.SQLSegmentsExtractorFactory;
import io.shardingsphere.core.parsing.antlr.filler.SQLStatementFillerEngine;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingUnsupportedException;
import io.shardingsphere.core.parsing.parser.sql.SQLParser;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

/**
 * Parsing engine for Antlr.
 *
 * @author duhongjun
 */
@RequiredArgsConstructor
public final class AntlrParsingEngine implements SQLParser {
    
    private final DatabaseType databaseType;
    
    private final String sql;
    
    private final ShardingRule shardingRule;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    private final SQLParserEngine sqlParserEngine = new SQLParserEngine();
    
    @Override
    public SQLStatement parse() {
        SQLAST ast = sqlParserEngine.parse(databaseType, sql);
        SQLSegmentsExtractor extractor = getExtractor(ast.getType());
        Collection<SQLSegment> sqlSegments = extractor.extract(ast.getParserRuleContext(), shardingRule, shardingTableMetaData);
        SQLStatement result = new SQLStatementFillerEngine(databaseType).fill(sqlSegments, sql, ast.getType(), shardingRule, shardingTableMetaData);
        extractor.postExtract(result, shardingTableMetaData);
        return result;
    }
    
    private SQLSegmentsExtractor getExtractor(final SQLStatementType sqlStatementType) {
        SQLSegmentsExtractor result = SQLSegmentsExtractorFactory.getInstance(databaseType, sqlStatementType);
        if (null == result) {
            throw new SQLParsingUnsupportedException(String.format("Unsupported SQL statement of `%s`", sqlStatementType));
        }
        return result;
    }
}
