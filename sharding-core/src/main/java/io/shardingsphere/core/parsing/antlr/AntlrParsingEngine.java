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
import io.shardingsphere.core.parsing.antlr.ast.SQLASTParserFactory;
import io.shardingsphere.core.parsing.antlr.extractor.statement.SQLSegmentsExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.SQLSegmentsExtractorFactory;
import io.shardingsphere.core.parsing.antlr.extractor.statement.SQLStatementType;
import io.shardingsphere.core.parsing.antlr.filler.SQLStatementFillerEngine;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingUnsupportedException;
import io.shardingsphere.core.parsing.parser.sql.SQLParser;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Collection;

/**
 * Parsing engine for Antlr.
 *
 * @author duhongjun
 */
@AllArgsConstructor
public final class AntlrParsingEngine implements SQLParser {
    
    private DatabaseType databaseType;
    
    private String sql;
    
    private ShardingRule shardingRule;
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public SQLStatement parse() {
        ParseTree rootNode = parseAST();
        SQLStatementType sqlStatementType = SQLStatementType.nameOf(rootNode.getClass().getSimpleName());
        SQLSegmentsExtractor extractor = getExtractor(sqlStatementType);
        Collection<SQLSegment> sqlSegments = extractor.extract((ParserRuleContext) rootNode, shardingRule, shardingTableMetaData);
        SQLStatement result = new SQLStatementFillerEngine(databaseType).fill(sqlSegments, sql, sqlStatementType, shardingRule, shardingTableMetaData);
        extractor.postExtract(result, shardingTableMetaData);
        return result;
    }
    
    private ParseTree parseAST() {
        ParseTree result = SQLASTParserFactory.newInstance(databaseType, sql).execute().getChild(0);
        if (result instanceof ErrorNode) {
            throw new SQLParsingUnsupportedException(String.format("Unsupported SQL of `%s`", sql));
        }
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
