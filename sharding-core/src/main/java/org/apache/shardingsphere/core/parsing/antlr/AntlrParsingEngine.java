/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parsing.antlr;

import java.util.Collection;

import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.antlr.extractor.SQLSegmentsExtractorEngine;
import org.apache.shardingsphere.core.parsing.antlr.filler.SQLStatementFillerEngine;
import org.apache.shardingsphere.core.parsing.antlr.optimizer.SQLStatementOptimizerEngine;
import org.apache.shardingsphere.core.parsing.antlr.parser.SQLAST;
import org.apache.shardingsphere.core.parsing.antlr.parser.SQLParserEngine;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLParser;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.rule.SQLStatementFillerRule;

/**
 * SQL parsing engine.
 *
 * @author duhongjun
 * @author zhangliang
 */
public final class AntlrParsingEngine implements SQLParser {
    
    private final SQLParserEngine parserEngine;
    
    private final SQLSegmentsExtractorEngine extractorEngine;
    
    private final SQLStatementFillerEngine fillerEngine; 
    
    private final SQLStatementOptimizerEngine optimizerEngine;
    
    public AntlrParsingEngine(final DatabaseType databaseType, final String sql, final SQLStatementFillerRule sqlStatementFillerRule, final ShardingTableMetaData shardingTableMetaData) {
        parserEngine = new SQLParserEngine(databaseType, sql);
        extractorEngine = new SQLSegmentsExtractorEngine();
        fillerEngine = new SQLStatementFillerEngine(sql, sqlStatementFillerRule, shardingTableMetaData);
        optimizerEngine = new SQLStatementOptimizerEngine(shardingTableMetaData);
    }
    
    @Override
    public SQLStatement parse() {
        SQLAST ast = parserEngine.parse();
        Collection<SQLSegment> sqlSegments = extractorEngine.extract(ast);
        SQLStatement result = fillerEngine.fill(sqlSegments, ast.getRule());
        optimizerEngine.optimize(ast.getRule(), result);
        return result;
    }
}
