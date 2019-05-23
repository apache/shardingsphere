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

package org.apache.shardingsphere.core.parse.parser;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parse.exception.SQLParsingException;
import org.apache.shardingsphere.core.parse.rule.registry.ParseRuleRegistry;
import org.apache.shardingsphere.core.parse.rule.registry.statement.SQLStatementRule;

/**
 * SQL parser engine.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLParserEngine {
    
    private final ParseRuleRegistry parseRuleRegistry;
    
    private final DatabaseType databaseType;
    
    private final String sql;
    
    /**
     * Parse SQL to abstract syntax tree.
     *
     * @return abstract syntax tree of SQL
     */
    public SQLAST parse() {
        ParseTree parseTree = SQLParserFactory.newInstance(databaseType, sql).execute().getChild(0);
        if (parseTree instanceof ErrorNode) {
            throw new SQLParsingException(String.format("Unsupported SQL of `%s`", sql));
        }
        Optional<SQLStatementRule> sqlStatementRule = parseRuleRegistry.findSQLStatementRule(databaseType, parseTree.getClass().getSimpleName());
        if (sqlStatementRule.isPresent()) {
            return new SQLAST((ParserRuleContext) parseTree, sqlStatementRule.get());
        }
        throw new SQLParsingException(String.format("Unsupported SQL of `%s`", sql));
    }
}
