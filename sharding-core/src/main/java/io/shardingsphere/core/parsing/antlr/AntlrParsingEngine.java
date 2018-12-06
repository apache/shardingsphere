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

import java.util.Collection;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.extractor.statement.SQLSegmentsExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.SQLSegmentsExtractorFactory;
import io.shardingsphere.core.parsing.antlr.filler.SQLStatementFillerEngine;
import io.shardingsphere.core.parsing.antlr.optimizer.SQLStatementOptimizerEngine;
import io.shardingsphere.core.parsing.antlr.parser.SQLAST;
import io.shardingsphere.core.parsing.antlr.parser.SQLParserEngine;
import io.shardingsphere.core.parsing.antlr.parser.SQLStatementType;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingUnsupportedException;
import io.shardingsphere.core.parsing.parser.sql.SQLParser;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * SQL parsing engine.
 *
 * @author duhongjun
 * @author zhangliang
 */
public final class AntlrParsingEngine implements SQLParser {
    
    private final DatabaseType databaseType;
    
    private final ShardingRule shardingRule;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    private final SQLParserEngine parserEngine;
    
    private final SQLStatementFillerEngine fillerEngine; 
    
    private final SQLStatementOptimizerEngine optimizerEngine;
    
    public AntlrParsingEngine(final DatabaseType databaseType, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        this.databaseType = databaseType;
        this.shardingRule = shardingRule;
        this.shardingTableMetaData = shardingTableMetaData;
        parserEngine = new SQLParserEngine(databaseType, sql);
        fillerEngine = new SQLStatementFillerEngine(sql, shardingRule, shardingTableMetaData);
        optimizerEngine = new SQLStatementOptimizerEngine(databaseType, shardingTableMetaData);
    }
    
    @Override
    public SQLStatement parse() {
        SQLAST ast = parserEngine.parse();
        SQLSegmentsExtractor extractor = getExtractor(ast.getType());
        Collection<SQLSegment> sqlSegments = extractor.extract(ast.getParserRuleContext(), shardingRule, shardingTableMetaData);
        SQLStatement result = fillerEngine.fill(sqlSegments, ast.getType());
        optimizerEngine.optimize(ast.getType(), result);
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
