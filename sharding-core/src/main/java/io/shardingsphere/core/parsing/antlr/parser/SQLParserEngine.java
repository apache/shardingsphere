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

package io.shardingsphere.core.parsing.antlr.parser;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antlr.parser.impl.SQLParserFactory;
import io.shardingsphere.core.parsing.antlr.rule.registry.ParsingRuleRegistry;
import io.shardingsphere.core.parsing.antlr.rule.registry.statement.SQLStatementRule;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingUnsupportedException;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * SQL parser engine.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLParserEngine {
    
    private final ParsingRuleRegistry parsingRuleRegistry = ParsingRuleRegistry.getInstance();
    
    private final DatabaseType databaseType;
    
    private final String sql;
    
    /**
     * Parse SQL to AST.
     * 
     * @return Abstract syntax tree of SQL
     */
    public SQLAST parse() {
        ParseTree parseTree = SQLParserFactory.newInstance(databaseType, sql).execute().getChild(0);
        if (parseTree instanceof ErrorNode) {
            throw new SQLParsingUnsupportedException(String.format("Unsupported SQL of `%s`", sql));
        }
        Optional<SQLStatementRule> sqlStatementRule = parsingRuleRegistry.findSQLStatementRule(databaseType, parseTree.getClass().getSimpleName());
        if (!sqlStatementRule.isPresent()) {
            throw new SQLParsingUnsupportedException(String.format("Unsupported SQL of `%s`", sql));
        }
        return new SQLAST((ParserRuleContext) parseTree, sqlStatementRule.get());
    }
}
