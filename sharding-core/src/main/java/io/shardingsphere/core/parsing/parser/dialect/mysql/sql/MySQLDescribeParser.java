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

package io.shardingsphere.core.parsing.parser.dialect.mysql.sql;

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.parser.clause.TableReferencesClauseParser;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.DescribeStatement;
import io.shardingsphere.core.parsing.parser.sql.dal.describe.AbstractDescribeParser;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Describe parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLDescribeParser extends AbstractDescribeParser {
    
    private final LexerEngine lexerEngine;
    
    private final TableReferencesClauseParser tableReferencesClauseParser;
    
    public MySQLDescribeParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        tableReferencesClauseParser = new TableReferencesClauseParser(shardingRule, lexerEngine);
    }
    
    @Override
    public DescribeStatement parse() {
        lexerEngine.nextToken();
        DescribeStatement result = new DescribeStatement();
        tableReferencesClauseParser.parseSingleTableWithoutAlias(result);
        return result;
    }
}
